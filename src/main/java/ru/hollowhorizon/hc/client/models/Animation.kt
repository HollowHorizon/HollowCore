package ru.hollowhorizon.hc.client.models

import jassimp.AiAnimation
import jassimp.AiScene
import net.minecraft.util.math.vector.Matrix4f


class Animation(scene: AiScene, model: RiggedModel, index: Int) {
    val name: String
    val length: Double
    val tPS: Double
    private val root: AssimpNodeData
    private val bones: MutableList<Bone> = ArrayList()
    var boneInfoMap: Map<String, BoneInfo> = HashMap()
        private set

    init {
        val animation = scene.animations[index]
        name = animation.name
        println("Animation: $name")
        length = animation.duration
        tPS = animation.ticksPerSecond
        root = AssimpNodeData()
        handleChildren(root, scene.getSceneRoot(AssimpJomlProvider()))
        findMissingBones(animation, model)
    }

    private fun handleChildren(data: AssimpNodeData, source: SourceNode) {
        data.name = source.name
        data.transformation = source.transformation
        for (child in source.getChildren()) {
            val newData = AssimpNodeData()
            handleChildren(newData, child)
            data.children.add(newData)
        }
    }

    private fun findMissingBones(animation: AiAnimation, model: RiggedModel) {
        val size = animation.numChannels
        val infoMap = model.getBones()
        var boneCount = model.boneCount
        for (i in 0 until size) {
            val node = animation.channels[i]
            val name = node.nodeName
            if (!infoMap.containsKey(name)) {
                infoMap.put(name, BoneInfo(boneCount, Matrix4f()))
                boneCount++
                if (boneCount >= 100) break // TODO
            }
            bones.add(Bone(name, infoMap[name]!!.id, node))
        }
        boneInfoMap = infoMap
    }

    fun findBone(name: String): Bone? {
        for (bone in bones) {
            if (bone.name.lowercase() == name.lowercase()) return bone
        }
        return null
    }

    fun getRoot(): AssimpNodeData {
        return root
    }
}