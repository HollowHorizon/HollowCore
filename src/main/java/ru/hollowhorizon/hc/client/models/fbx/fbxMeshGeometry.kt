package ru.hollowhorizon.hc.client.models.fbx

import de.fabmax.kool.math.Vec2f
import de.fabmax.kool.math.Vec3d
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.math.Vec4f
import ru.hollowhorizon.hc.HollowCore
import kotlin.reflect.KMutableProperty0

open class Geometry(id: Long, element: Element, name: String, doc: Document) : Object(id, element, name) {

    /** Get the Skin attached to this geometry or NULL */
    var skin: Skin? = null

    init {
        val conns = doc.getConnectionsByDestinationSequenced(id, "Deformer")
        for (con in conns) {
            val sk = processSimpleConnection<Skin>(con, false, "Skin -> Geometry", element)
            if (sk != null) {
                skin = sk
                break
            }
        }
    }
}


const val AI_MAX_NUMBER_OF_TEXTURECOORDS = 0x8
const val AI_MAX_NUMBER_OF_COLOR_SETS = 0x8

/**
 *  DOM class for FBX geometry of type "Mesh"
 */
class MeshGeometry(id: Long, element: Element, name: String, doc: Document) : Geometry(id, element, name, doc) {

    // cached data arrays
    /** Per-face-vertex material assignments */
    val materials = ArrayList<Int>()
    val vertices = ArrayList<Vec3f>()
    val faces = ArrayList<Int>()
    val facesVertexStartIndices = ArrayList<Int>()
    val tangents = ArrayList<Vec3f>()
    val binormals = ArrayList<Vec3f>()
    val normals = ArrayList<Vec3f>()

    val indeces = ArrayList<Int>()

    val uvNames = Array(AI_MAX_NUMBER_OF_TEXTURECOORDS) { "" }
    val uvs = Array(AI_MAX_NUMBER_OF_TEXTURECOORDS) { ArrayList<Vec2f>() }
    val colors = Array(AI_MAX_NUMBER_OF_COLOR_SETS) { ArrayList<Vec4f>() }

    var mappingCounts = intArrayOf()
    var mappingOffsets = intArrayOf()
    var mappings = intArrayOf()

    val triangles = ArrayList<Int>()


    init {
        val sc = element.compound ?: domError("failed to read Geometry object (class: Mesh), no data scope found")

        // must have Mesh elements:
        val vertices = getRequiredElement(sc, "Vertices", element)
        val polygonVertexIndex = getRequiredElement(sc, "PolygonVertexIndex", element)

        // optional Mesh elements:
        val layer = sc.getCollection("Layer")

        val tempVerts = ArrayList<Vec3f>()
        vertices.parseVec3DataArray(tempVerts)

        if (tempVerts.isEmpty())
            HollowCore.LOGGER.warn("encountered mesh with no vertices")
        else {

            val tempFaces = ArrayList<Int>()
            polygonVertexIndex.parseIntsDataArray(tempFaces)

            if (tempFaces.isEmpty())
                HollowCore.LOGGER.warn("encountered mesh with no faces")
            else {
                this.vertices.ensureCapacity(tempFaces.size)
                faces.ensureCapacity(tempFaces.size / 3)

                mappingOffsets = IntArray(tempVerts.size)
                mappingCounts = IntArray(tempVerts.size)
                mappings = IntArray(tempFaces.size)

                val vertexCount = tempVerts.size

                // generate output vertices, computing an adjacency table to preserve the mapping from fbx indices to *this* indexing.
                var count = 0
                for (index in tempFaces) {
                    val absi = if (index < 0) -index - 1 else index
                    if (absi >= vertexCount) domError("polygon vertex index out of range", polygonVertexIndex)

                    this.vertices += tempVerts[absi]
                    ++count

                    ++mappingCounts[absi]

                    if (index < 0) {
                        faces += count
                        count = 0
                    }
                }

                var cursor = 0
                for (i in tempVerts.indices) {
                    mappingOffsets[i] = cursor
                    cursor += mappingCounts[i]

                    mappingCounts[i] = 0
                }

                cursor = 0
                for (index in tempFaces) {
                    val absi = if (index < 0) -index - 1 else index
                    mappings[mappingOffsets[absi] + mappingCounts[absi]++] = cursor++
                }

                // if settings.readAllLayers is true:
                //  * read all layers, try to load as many vertex channels as possible
                // if settings.readAllLayers is false:
                //  * read only the layer with index 0, but warn about any further layers
                layer.forEach {
                    val tokens = it.tokens

                    val index = tokens[0].parseAsInt

                    readLayer(it.scope)
                }
            }
        }

        generateTriangles()
    }

    private fun generateTriangles() {
        var vertexIndex = 0
        for (faceVertexCount in faces) {
            if (faceVertexCount < 3) {
                HollowCore.LOGGER.warn("skipping face with < 3 vertices")
                vertexIndex += faceVertexCount
                continue
            }

            for (i in 0 until faceVertexCount - 2) {
                triangles.add(vertexIndex)
                triangles.add(vertexIndex + i + 1)
                triangles.add(vertexIndex + i + 2)
            }
            vertexIndex += faceVertexCount
        }
    }

    /** Get a UV coordinate slot, returns an empty array if
     *  the requested slot does not exist. */
    fun getTextureCoords(index: Int) = if (index >= AI_MAX_NUMBER_OF_TEXTURECOORDS) arrayListOf() else uvs[index]

    /** Get a UV coordinate slot, returns an empty array if the requested slot does not exist. */
    fun getTextureCoordChannelName(index: Int) = if (index >= AI_MAX_NUMBER_OF_TEXTURECOORDS) "" else uvNames[index]

    /** Get a vertex color coordinate slot, returns an empty array if the requested slot does not exist. */
    fun getVertexColors(index: Int) = if (index >= AI_MAX_NUMBER_OF_COLOR_SETS) arrayListOf() else colors[index]

    /** Convert from a fbx file vertex index (for example from a #Cluster weight) or NULL if the vertex index is not valid. */
    fun toOutputVertexIndex(inIndex: Int, count: KMutableProperty0<Int>): Int? {
        if (inIndex >= mappingCounts.size) return null

        assert(mappingCounts.size == mappingOffsets.size)
        count.set(mappingCounts[inIndex])

        assert(mappingOffsets[inIndex] + count() <= mappings.size)

        return mappingOffsets[inIndex]
    }

    /** Determine the face to which a particular output vertex index belongs. This mapping is always unique. */
    fun faceForVertexIndex(inIndex: Int): Int {
        assert(inIndex < vertices.size)

        // in the current conversion pattern this will only be needed if weights are present, so no need to always pre-compute this table
        if (facesVertexStartIndices.isEmpty()) {
            for (i in 0..faces.size)
                facesVertexStartIndices += 0

            facesVertexStartIndices[1] = faces.sum()
            facesVertexStartIndices.removeAt(facesVertexStartIndices.lastIndex)
        }

        assert(facesVertexStartIndices.size == faces.size)
        return facesVertexStartIndices.indexOfFirst { it > inIndex } // TODO last item excluded?
    }

    fun readLayer(layer: Scope) {
        val layerElement = layer.getCollection("LayerElement")
        for (eit in layerElement)
            readLayerElement(eit.scope)
    }

    fun readLayerElement(layerElement: Scope) {
        val type = getRequiredElement(layerElement, "Type")
        val typedIndex = getRequiredElement(layerElement, "TypedIndex")

        val type_ = type[0].parseAsString
        val typedIndex_ = typedIndex[0].parseAsInt

        val top = element.scope
        val candidates = top.getCollection(type_)

        for (it in candidates) {
            val index = it[0].parseAsInt
            if (index == typedIndex_) {
                readVertexData(type_, typedIndex_, it.scope)
                return
            }
        }

        HollowCore.LOGGER.error("failed to resolve vertex layer element: $type, index: $typedIndex")
    }

    fun readVertexData(type: String, index: Int, source: Scope) {

        val mappingInformationType = getRequiredElement(source, "MappingInformationType")[0].parseAsString

        val referenceInformationType = getRequiredElement(source, "ReferenceInformationType")[0].parseAsString

        when (type) {
            "LayerElementUV" -> {
                if (index >= AI_MAX_NUMBER_OF_TEXTURECOORDS) {
                    HollowCore.LOGGER.error("ignoring UV layer, maximum number of UV channels exceeded: $index (limit is $AI_MAX_NUMBER_OF_TEXTURECOORDS)")
                    return
                }
                source["Name"]?.let { uvNames[index] = it[0].parseAsString }

                readVertexDataUV(uvs[index], source, mappingInformationType, referenceInformationType)
            }
            "LayerElementMaterial" -> {
                if (materials.isNotEmpty()) {
                    HollowCore.LOGGER.error("ignoring additional material layer")
                    return
                }
                val tempMaterials = ArrayList<Int>()

                readVertexDataMaterials(tempMaterials, source, mappingInformationType, referenceInformationType)

                /*  sometimes, there will be only negative entries. Drop the material layer in such a case (I guess it
                    means a default material should be used). This is what the converter would do anyway, and it avoids
                    losing the material if there are more material layers coming of which at least one contains actual
                    data (did observe that with one test file). */
                if (tempMaterials.all { it < 0 }) {
                    HollowCore.LOGGER.warn("ignoring dummy material layer (all entries -1)")
                    return
                }
                materials.clear()
                materials += tempMaterials
            }
            "LayerElementNormal" -> {
                if (normals.isNotEmpty()) {
                    HollowCore.LOGGER.error("ignoring additional normal layer")
                    return
                }
                readVertexDataNormals(normals, source, mappingInformationType, referenceInformationType)
            }
            "LayerElementTangent" -> {
                if (tangents.isNotEmpty()) {
                    HollowCore.LOGGER.error("ignoring additional tangent layer")
                    return
                }

                readVertexDataTangents(tangents, source, mappingInformationType, referenceInformationType)
            }
            "LayerElementBinormal" -> {
                if (binormals.isNotEmpty()) {
                    HollowCore.LOGGER.error("ignoring additional binormal layer")
                    return
                }

                readVertexDataBinormals(binormals, source, mappingInformationType, referenceInformationType)
            }
            "LayerElementColor" -> {
                if (index >= AI_MAX_NUMBER_OF_COLOR_SETS) {
                    HollowCore.LOGGER.error("ignoring vertex color layer, maximum number of color sets exceeded: $index (limit is $AI_MAX_NUMBER_OF_COLOR_SETS)")
                    return
                }

                readVertexDataColors(colors[index], source, mappingInformationType, referenceInformationType)
            }
        }
    }

    fun readVertexDataUV(uvOut: ArrayList<Vec2f>, source: Scope, mappingInformationType: String, referenceInformationType: String) =
        resolveVertexDataArray(uvOut, source, mappingInformationType, referenceInformationType, "UV",
            "UVIndex", vertices.size, mappingCounts, mappingOffsets, mappings)

    fun readVertexDataNormals(normalsOut: ArrayList<Vec3f>, source: Scope, mappingInformationType: String, referenceInformationType: String) =
        resolveVertexDataArray(normalsOut, source, mappingInformationType, referenceInformationType, "Normals",
            "NormalsIndex", vertices.size, mappingCounts, mappingOffsets, mappings)

    fun readVertexDataColors(colorsOut: ArrayList<Vec4f>, source: Scope, mappingInformationType: String, referenceInformationType: String) =
        resolveVertexDataArray(colorsOut, source, mappingInformationType, referenceInformationType, "Colors",
            "ColorIndex", vertices.size, mappingCounts, mappingOffsets, mappings)

    fun readVertexDataTangents(tangentsOut: ArrayList<Vec3f>, source: Scope, mappingInformationType: String, referenceInformationType: String) {
        val any = source.elements["Tangents"]!!.isNotEmpty()
        resolveVertexDataArray(tangentsOut, source, mappingInformationType, referenceInformationType, if (any) "Tangents" else "Tangent",
            if (any) "TangentsIndex" else "TangentIndex", vertices.size, mappingCounts, mappingOffsets, mappings)
    }

    fun readVertexDataBinormals(binormalsOut: ArrayList<Vec3f>, source: Scope, mappingInformationType: String, referenceInformationType: String) {
        val any = source.elements["Binormals"]!!.isNotEmpty()
        resolveVertexDataArray(binormalsOut, source, mappingInformationType, referenceInformationType, if (any) "Binormals" else "Binormal",
            if (any) "BinormalsIndex" else "BinormalIndex", vertices.size, mappingCounts, mappingOffsets, mappings)
    }

    fun readVertexDataMaterials(materialsOut: ArrayList<Int>, source: Scope, mappingInformationType: String, referenceInformationType: String) {
        val faceCount = faces.size
        assert(faceCount != 0)

        /*  materials are handled separately. First of all, they are assigned per-face and not per polyvert. Secondly,
            ReferenceInformationType=IndexToDirect has a slightly different meaning for materials. */
        getRequiredElement(source, "Materials").parseIntsDataArray(materialsOut)

        if (mappingInformationType == "AllSame") {
            // easy - same material for all faces
            if (materialsOut.isEmpty()) {
                HollowCore.LOGGER.error("expected material index, ignoring")
                return
            } else if (materialsOut.size > 1) {
                HollowCore.LOGGER.warn("expected only a single material index, ignoring all except the first one")
                materialsOut.clear()
            }
            for (i in vertices.indices)
                materials += materialsOut[0]
        } else if (mappingInformationType == "ByPolygon" && referenceInformationType == "IndexToDirect") {
            if (materials.size < faceCount) while (materials.size < faceCount) materials += 0
            else while (materials.size > faceCount) materials.dropLast(materials.size - faceCount)

            if (materialsOut.size != faceCount) {
                HollowCore.LOGGER.error("length of input data unexpected for ByPolygon mapping: ${materialsOut.size}, expected $faceCount")
                return
            }
        } else
            HollowCore.LOGGER.error("ignoring material assignments, access type not implemented: $mappingInformationType, $referenceInformationType")
    }

    /** Lengthy utility function to read and resolve a FBX vertex data array - that is, the output is in polygon vertex
     *  order. This logic is used for reading normals, UVs, colors, tangents .. */
    inline fun <reified T : Any> resolveVertexDataArray(dataOut: ArrayList<T>, source: Scope, mappingInformationType: String,
                                                        referenceInformationType: String, dataElementName: String,
                                                        indexDataElementName: String, vertexCount: Int,
                                                        mappingCounts: IntArray, mappingOffsets: IntArray, mappings: IntArray) {

        /*  handle permutations of Mapping and Reference type - it would be nice to deal with this more elegantly and
            with less redundancy, but right now it seems unavoidable. */
        if (mappingInformationType == "ByVertice" && referenceInformationType == "Direct") {

            if (!source.hasElement(indexDataElementName)) return

            val tempData = ArrayList<T>()
            getRequiredElement(source, dataElementName).parseVectorDataArray(tempData)

            val tempData2 = Array<T?>(tempData.size, { null })
            for (i in 0 until tempData.size) {
                val iStart = mappingOffsets[i]
                val iEnd = iStart + mappingCounts[i]
                for (j in iStart until iEnd)
                    tempData2[mappings[j]] = tempData[i]
            }
            dataOut.addAll(tempData2.filterNotNull())
        } else if (mappingInformationType == "ByVertice" && referenceInformationType == "IndexToDirect") {
            val tempData = ArrayList<T>()
            getRequiredElement(source, dataElementName).parseVectorDataArray(tempData)

            val tempData2 = Array<T?>(tempData.size, { null })

            val uvIndices = ArrayList<Int>()
            if (!source.hasElement(indexDataElementName)) return
            getRequiredElement(source, indexDataElementName).parseIntsDataArray(uvIndices)

            for (i in 0 until uvIndices.size) {
                val iStart = mappingOffsets[i]
                val iEnd = iStart + mappingCounts[i]
                for (j in iStart until iEnd) {
                    if (uvIndices[i] >= tempData.size)
                        domError("index out of range", getRequiredElement(source, indexDataElementName))
                    tempData2[mappings[j]] = tempData[uvIndices[i]]
                }
            }
            dataOut.addAll(tempData2.filterNotNull())
        } else if (mappingInformationType == "ByPolygonVertex" && referenceInformationType == "Direct") {
            val tempData = ArrayList<T>()
            getRequiredElement(source, dataElementName).parseVectorDataArray(tempData)

            if (tempData.size != vertexCount) {
                HollowCore.LOGGER.error("length of input data unexpected for ByPolygon mapping: ${tempData.size}, expected $vertexCount")
                return
            }
            dataOut.addAll(tempData)
        } else if (mappingInformationType == "ByPolygonVertex" && referenceInformationType == "IndexToDirect") {
            val tempData = ArrayList<T>()
            getRequiredElement(source, dataElementName).parseVectorDataArray(tempData)

            val uvIndices = ArrayList<Int>()
            getRequiredElement(source, indexDataElementName).parseIntsDataArray(uvIndices)

            if (uvIndices.size != vertexCount) {
                HollowCore.LOGGER.error("length of input data unexpected for ByPolygonVertex mapping")
                return
            }

            val tempData2 = Array<T?>(vertexCount) { null }
            var next = 0
            for (i in uvIndices) {
                if (i >= tempData.size)
                    domError("index out of range", getRequiredElement(source, indexDataElementName))

                tempData2[next++] = tempData[i]
            }
            dataOut.addAll(tempData2.filterNotNull())
        } else if(mappingInformationType == "ByPolygon" && referenceInformationType == "Direct") {
            val tempData = ArrayList<T>()
            getRequiredElement(source, dataElementName).parseVectorDataArray(tempData)

            if (tempData.size != faces.size) {
                HollowCore.LOGGER.error("length of input data unexpected for ByPolygon mapping: ${tempData.size}, expected ${faces.size}")
                return
            }

            // Precompute face start indices
            val faceStartIndices = IntArray(faces.size)
            var start = 0
            for (i in faces.indices) {
                faceStartIndices[i] = start
                start += faces[i]
            }

            val tempData2 = arrayOfNulls<T>(vertexCount)
            for (i in faces.indices) {
                val faceValue = tempData[i]
                val vertexStart = faceStartIndices[i]
                for (j in 0 until faces[i]) {
                    tempData2[vertexStart + j] = faceValue
                }
            }
            dataOut.addAll(tempData2.filterNotNull())
        } else if (mappingInformationType == "ByPolygon" && referenceInformationType == "IndexToDirect") {
            val tempData = ArrayList<T>()
            getRequiredElement(source, dataElementName).parseVectorDataArray(tempData)

            val indices = ArrayList<Int>()
            getRequiredElement(source, indexDataElementName).parseIntsDataArray(indices)

            if (indices.size != faces.size) {
                HollowCore.LOGGER.error("length of index data unexpected for ByPolygon mapping: ${indices.size}, expected ${faces.size}")
                return
            }

            // Precompute face start indices
            val faceStartIndices = IntArray(faces.size)
            var start = 0
            for (i in faces.indices) {
                faceStartIndices[i] = start
                start += faces[i]
            }

            val tempData2 = arrayOfNulls<T>(vertexCount)
            for (i in faces.indices) {
                val index = indices[i]
                if (index < 0 || index >= tempData.size) {
                    HollowCore.LOGGER.error("index out of range in ByPolygon/IndexToDirect mapping: $index, data array size ${tempData.size}")
                    continue
                }
                val faceValue = tempData[index]
                val vertexStart = faceStartIndices[i]
                for (j in 0 until faces[i]) {
                    tempData2[vertexStart + j] = faceValue
                }
            }
            dataOut.addAll(tempData2.filterNotNull())
        }
        else
            HollowCore.LOGGER.error("ignoring vertex data channel, access type not implemented: $mappingInformationType, $referenceInformationType")
    }
}