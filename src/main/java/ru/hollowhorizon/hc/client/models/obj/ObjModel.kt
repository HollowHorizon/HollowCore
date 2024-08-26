package ru.hollowhorizon.hc.client.models.obj

import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.models.internal.Material
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.stream

internal class ObjData(var url: String) {
    val groups = arrayListOf<ObjGroup>()
    val vertices = arrayListOf<Float>()
    val vertexNormals = arrayListOf<Float>()
    val vertexTextures = arrayListOf<Float>()

    val materials = hashMapOf<String, Material>()

    val currentGroup: ObjGroup
        get() {
            if (groups.isEmpty())
                groups += ObjGroup("default")

            return groups.last()
        }
}

internal class ObjGroup(val name: String) {
    val subGroups = arrayListOf(SubGroup())

    val currentSubGroup
        get() = subGroups.last()
}

internal class SubGroup {
    val faces = arrayListOf<Int>()
    val smoothingGroups = arrayListOf<Int>()

    var material = Material()

    var smoothingGroup = -1
}

internal class MtlData(val url: ResourceLocation) {

    val materials = hashMapOf<String, Material>()
}

class ObjModelLoader {

    companion object {
        private val objParsers = linkedMapOf<(String) -> Boolean, (List<String>, ObjData) -> Unit>()
        private val mtlParsers = linkedMapOf<(String) -> Boolean, (List<String>, MtlData) -> Unit>()

        init {
            objParsers[{ it.startsWith("g") }] = Companion::parseGroup
            objParsers[{ it.startsWith("s") }] = Companion::parseSmoothing
            objParsers[{ it.startsWith("vt") }] = Companion::parseVertexTextures
            objParsers[{ it.startsWith("vn") }] = Companion::parseVertexNormals
            objParsers[{ it.startsWith("v ") }] = Companion::parseVertices
            objParsers[{ it.startsWith("f") }] = Companion::parseFaces
            objParsers[{ it.startsWith("mtllib") }] = Companion::parseMaterialLib
            objParsers[{ it.startsWith("usemtl") }] = Companion::parseUseMaterial

            mtlParsers[{ it.startsWith("newmtl") }] = Companion::parseNewMaterial
        }

        private fun parseGroup(tokens: List<String>, data: ObjData) {
            val groupName = if (tokens.isEmpty()) "default" else tokens[0]

            data.groups += ObjGroup(groupName)
        }

        private fun parseSmoothing(tokens: List<String>, data: ObjData) {
            data.currentGroup.currentSubGroup.smoothingGroup = tokens.toSmoothingGroup()
        }

        private fun parseVertexTextures(tokens: List<String>, data: ObjData) {
            data.vertexTextures += tokens.toFloats2()
        }

        private fun parseVertexNormals(tokens: List<String>, data: ObjData) {
            data.vertexNormals += tokens.toFloats3()
        }

        private fun parseVertices(tokens: List<String>, data: ObjData) {
            // for -Y
            // .mapIndexed { index, fl -> if (index == 1) -fl else fl }
            data.vertices += tokens.toFloats3()
        }

        private fun parseFaces(tokens: List<String>, data: ObjData) {
            if (tokens.size > 3) {
                for (i in 2 until tokens.size) {
                    parseFaceVertex(tokens[0], data)
                    parseFaceVertex(tokens[i - 1], data)
                    parseFaceVertex(tokens[i], data)
                }
            } else {
                tokens.forEach { token ->
                    parseFaceVertex(token, data)
                }
            }
        }

        private fun parseFaceVertex(token: String, data: ObjData) {
            val faceVertex = token.split("/")

            // JavaFX format is vertices, normals and tex
            when (faceVertex.size) {
                // f v1
                1 -> {
                    data.currentGroup.currentSubGroup.faces += faceVertex[0].toInt() - 1
                    // add vt1 as 0
                    data.currentGroup.currentSubGroup.faces += 0
                }

                // f v1/vt1
                2 -> {
                    data.currentGroup.currentSubGroup.faces += faceVertex[0].toInt() - 1
                    // add vt1
                    data.currentGroup.currentSubGroup.faces += faceVertex[1].toInt() - 1
                }

                // f v1//vn1
                // f v1/vt1/vn1
                3 -> {
                    data.currentGroup.currentSubGroup.faces += faceVertex[0].toInt() - 1
                    data.currentGroup.currentSubGroup.faces += faceVertex[2].toInt() - 1
                    // add vt1 if present, else 0
                    data.currentGroup.currentSubGroup.faces += (faceVertex[1].toIntOrNull() ?: 1) - 1
                }
            }
        }

        private fun parseMaterialLib(tokens: List<String>, data: ObjData) {
            val fileName = tokens[0]
            val mtlURL = data.url.substringBeforeLast('/') + '/' + fileName

            val mtlData = loadMtlData(mtlURL.rl)

            data.materials += mtlData.materials
        }

        private fun parseUseMaterial(tokens: List<String>, data: ObjData) {
            data.currentGroup.subGroups += SubGroup()

            data.currentGroup.currentSubGroup.material = data.materials[tokens[0]]
                ?: throw RuntimeException("Material with name ${tokens[0]} not found")

        }

        private fun List<String>.toFloats2(): List<Float> {
            return this.take(2).map { it.toFloat() }
        }

        private fun List<String>.toFloats3(): List<Float> {
            return this.take(3).map { it.toFloat() }
        }

        private fun List<String>.toSmoothingGroup(): Int {
            return if (this[0] == "off") 0 else this[0].toInt()
        }

        private fun parseNewMaterial(tokens: List<String>, data: MtlData) {
            data.materials[tokens[0]] = Material()
        }

        private fun loadObjData(url: String): ObjData {
            val data = ObjData(url)

            load(url.rl, objParsers, data)

            return data
        }

        private fun loadMtlData(url: ResourceLocation): MtlData {
            val data = MtlData(url)

            load(url, mtlParsers, data)

            return data
        }

        private fun <T> load(
            location: ResourceLocation,
            parsers: Map<(String) -> Boolean, (List<String>, T) -> Unit>,
            data: T,
        ) {

            location.stream.bufferedReader().useLines {
                it.forEach { line ->

                    val lineTrimmed = line.trim()

                    for ((condition, action) in parsers) {
                        if (condition.invoke(lineTrimmed)) {
                            // drop identifier
                            val tokens = lineTrimmed.split(" +".toRegex()).drop(1)

                            action.invoke(tokens, data)
                            break
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val model = loadObjData("hollowcore:models/model.obj")
            println(model)
        }
    }
}