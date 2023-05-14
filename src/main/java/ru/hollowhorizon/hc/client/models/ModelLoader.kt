package ru.hollowhorizon.hc.client.models

import org.lwjgl.PointerBuffer
import org.lwjgl.assimp.AIMesh
import org.lwjgl.assimp.AIScene
import org.lwjgl.assimp.Assimp.*


object ModelLoader {


    @JvmStatic
    fun main(args: Array<String>) {
        val scene = aiImportFile(
            "C:\\Users\\user\\Downloads\\gate.geo.fbx",
            aiProcess_GenSmoothNormals or
                    aiProcess_JoinIdenticalVertices or
                    aiProcess_Triangulate or
                    aiProcess_FixInfacingNormals or
                    aiProcess_LimitBoneWeights or
                    aiProcess_OptimizeMeshes
        )

        if(scene == null) {
            println("Error: "+aiGetErrorString())
            return
        }

        val model = HollowModel(scene)

        print(model)
    }

    fun loadMesh(scene: AIScene) {
        val numMeshes: Int = scene.mNumMeshes()
        if(numMeshes == 0) return
        val aiMeshes: PointerBuffer = scene.mMeshes()!!
        val meshes = mutableListOf<AIMesh>()
        for(i in 0 until numMeshes) {
            meshes.add(AIMesh.create(aiMeshes[i]))
        }

        println(meshes)
    }
}