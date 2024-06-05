package ru.hollowhorizon.hc.client.models.obj

import net.minecraft.resources.ResourceLocation
import org.joml.Vector2f
import org.joml.Vector3f
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.GltfTree.Mesh
import ru.hollowhorizon.hc.client.utils.stream

class ObjModel(val location: ResourceLocation) {
    init {
        val vertices = mutableListOf<Vector3f>()
        val textures = mutableListOf<Vector2f>()
        val normals = mutableListOf<Vector3f>()
        val faces = mutableListOf<GltfTree.Primitive>()

        location.stream.bufferedReader().lines().forEach { line ->
            val parts = line.split("\\s+".toRegex())
            when (parts[0]) {
                "v" -> vertices.add(Vector3f(parts[1].toFloat(), parts[2].toFloat(), parts[3].toFloat()))
                "vt" -> textures.add(Vector2f(parts[1].toFloat(), parts[2].toFloat()))
                "vn" -> normals.add(Vector3f(parts[1].toFloat(), parts[2].toFloat(), parts[3].toFloat()))
                "f" -> {
                    val vertexIndices = mutableListOf<Int>()
                    val textureIndices = mutableListOf<Int>()
                    val normalIndices = mutableListOf<Int>()

                    parts.drop(1).forEach { part ->
                        val indices = part.split("/")
                        vertexIndices.add(indices[0].toInt() - 1)
                        if (indices.size > 1 && indices[1].isNotEmpty()) textureIndices.add(indices[1].toInt() - 1)
                        if (indices.size > 2 && indices[2].isNotEmpty()) normalIndices.add(indices[2].toInt() - 1)
                    }

                }
            }
        }
    }
}