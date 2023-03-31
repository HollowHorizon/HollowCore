package ru.hollowhorizon.hc.client.models

import net.minecraft.util.math.vector.Vector4f
//import org.lwjgl.assimp.*
//import org.lwjgl.assimp.Assimp.*
import java.nio.IntBuffer


object StaticMeshesLoader {
//    fun listIntToArray(list: List<Int>): IntArray {
//        return list.stream().mapToInt { v -> v }.toArray()
//    }
//
//    fun listToArray(list: List<Float>): FloatArray {
//        val size = list.size
//        val floatArr = FloatArray(size)
//        for (i in 0 until size) {
//            floatArr[i] = list[i]
//        }
//        return floatArr
//    }
//
//    @JvmOverloads
//    @Throws(Exception::class)
//    fun load(
//        resourcePath: String, texturesDir: String? = null,
//        flags: Int =
//            aiProcess_GenSmoothNormals or aiProcess_JoinIdenticalVertices or aiProcess_Triangulate
//                    or aiProcess_FixInfacingNormals or aiProcess_PreTransformVertices,
//    ): Array<Mesh?> {
//        val aiScene: AIScene = aiImportFile(resourcePath, flags) ?: throw Exception("Error loading model")
//        val numMaterials = aiScene.mNumMaterials()
//        val aiMaterials = aiScene.mMaterials()
//        val materials: MutableList<Material> = ArrayList()
//        for (i in 0 until numMaterials) {
//            val aiMaterial = AIMaterial.create(aiMaterials!![i])
//            processMaterial(aiMaterial, materials, texturesDir)
//        }
//        val numMeshes = aiScene.mNumMeshes()
//        val aiMeshes = aiScene.mMeshes()
//        val meshes = arrayOfNulls<Mesh>(numMeshes)
//        for (i in 0 until numMeshes) {
//            val aiMesh = AIMesh.create(aiMeshes!![i])
//            val mesh = processMesh(aiMesh, materials)
//            meshes[i] = mesh
//        }
//        return meshes
//    }
//
//    internal fun processIndices(aiMesh: AIMesh, indices: MutableList<Int>) {
//        val numFaces = aiMesh.mNumFaces()
//        val aiFaces = aiMesh.mFaces()
//        for (i in 0 until numFaces) {
//            val aiFace = aiFaces[i]
//            val buffer: IntBuffer = aiFace.mIndices()
//            while (buffer.remaining() > 0) {
//                indices.add(buffer.get())
//            }
//        }
//    }
//
//    @Throws(Exception::class)
//    internal fun processMaterial(
//        aiMaterial: AIMaterial, materials: MutableList<Material>,
//        texturesDir: String?,
//    ) {
//        val colour = AIColor4D.create()
//        val path = AIString.calloc()
//        aiGetMaterialTexture(
//            aiMaterial, aiTextureType_DIFFUSE, 0, path, null as IntBuffer?,
//            null, null, null, null, null
//        )
//        val textPath = path.dataString()
//        var texture: Texture? = null
//        if (textPath.isNotEmpty()) {
//            val textCache = TextureCache.instance
//            var textureFile = ""
//            if (texturesDir?.isNotEmpty() == true) {
//                textureFile += "$texturesDir/"
//            }
//            textureFile += textPath
//            textureFile = textureFile.replace("//", "/")
//            texture = textCache.getTexture(textureFile)
//        }
//        var ambient = Material.DEFAULT_COLOUR
//        var result: Int = aiGetMaterialColor(
//            aiMaterial, AI_MATKEY_COLOR_AMBIENT, aiTextureType_NONE, 0,
//            colour
//        )
//        if (result == 0) {
//            ambient = Vector4f(colour.r(), colour.g(), colour.b(), colour.a())
//        }
//        var diffuse = Material.DEFAULT_COLOUR
//        result = aiGetMaterialColor(
//            aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0,
//            colour
//        )
//        if (result == 0) {
//            diffuse = Vector4f(colour.r(), colour.g(), colour.b(), colour.a())
//        }
//        var specular = Material.DEFAULT_COLOUR
//        result = aiGetMaterialColor(
//            aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0,
//            colour
//        )
//        if (result == 0) {
//            specular = Vector4f(colour.r(), colour.g(), colour.b(), colour.a())
//        }
//        val material = Material(ambient, diffuse, specular, 1.0f)
//        material.texture = texture
//        materials.add(material)
//    }
//
//    private fun processMesh(aiMesh: AIMesh, materials: List<Material?>): Mesh {
//        val vertices: MutableList<Float> = ArrayList()
//        val textures: MutableList<Float> = ArrayList()
//        val normals: MutableList<Float> = ArrayList()
//        val indices: MutableList<Int> = ArrayList()
//        processVertices(aiMesh, vertices)
//        processNormals(aiMesh, normals)
//        processTextCoords(aiMesh, textures)
//        processIndices(aiMesh, indices)
//
//        // Texture coordinates may not have been populated. We need at least the empty slots
//        if (textures.size == 0) {
//            val numElements = vertices.size / 3 * 2
//            for (i in 0 until numElements) {
//                textures.add(0.0f)
//            }
//        }
//        val mesh = Mesh(
//            listToArray(vertices), listToArray(textures),
//            listToArray(normals), listIntToArray(indices)
//        )
//        val material: Material?
//        val materialIdx = aiMesh.mMaterialIndex()
//        material = if (materialIdx >= 0 && materialIdx < materials.size) {
//            materials[materialIdx]
//        } else {
//            Material()
//        }
//        mesh.material = material
//        return mesh
//    }
//
//    internal fun processNormals(aiMesh: AIMesh, normals: MutableList<Float>) {
//        val aiNormals = aiMesh.mNormals()
//        while (aiNormals != null && aiNormals.remaining() > 0) {
//            val aiNormal = aiNormals.get()
//            normals.add(aiNormal.x())
//            normals.add(aiNormal.y())
//            normals.add(aiNormal.z())
//        }
//    }
//
//    internal fun processTextCoords(aiMesh: AIMesh, textures: MutableList<Float>) {
//        val textCoords = aiMesh.mTextureCoords(0)
//        val numTextCoords = textCoords?.remaining() ?: 0
//        for (i in 0 until numTextCoords) {
//            val textCoord = textCoords!!.get()
//            textures.add(textCoord.x())
//            textures.add(1 - textCoord.y())
//        }
//    }
//
//    internal fun processVertices(aiMesh: AIMesh, vertices: MutableList<Float>) {
//        val aiVertices = aiMesh.mVertices()
//        while (aiVertices.remaining() > 0) {
//            val aiVertex = aiVertices.get()
//            vertices.add(aiVertex.x())
//            vertices.add(aiVertex.y())
//            vertices.add(aiVertex.z())
//        }
//    }
}
