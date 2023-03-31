package ru.hollowhorizon.hc.client.models

import net.minecraft.util.math.vector.Matrix4f

class AnimationWrapper(private val animation: Animation? = null) {
    var currentTime = 0.0
    var deltaTime = 0.0
    val matrices = arrayOfNulls<Matrix4f>(100).apply {
        for (i in 0..99) {
            this[i] = Matrix4f() // identity matrix
        }
    }.requireNoNulls()

    fun update(partialTicks: Double) {
        deltaTime = partialTicks
        currentTime += partialTicks * animation!!.tPS
        currentTime %= animation.length
        calculateBoneTransformations(animation.getRoot(), Matrix4f())
    }

    fun calculateBoneTransformations(node: AssimpNodeData, parent: Matrix4f?) {
        val name = node.name
        var transform: Matrix4f = node.transformation
        val bone = animation!!.findBone(name!!)
        if (bone != null) {
            bone.update(currentTime)
            transform = bone.getLocalTransformation()
        }
        val globalTransform: Matrix4f = transform.apply { multiply(parent) }
        val map: Map<String, BoneInfo> = animation.boneInfoMap
        if (map.isNotEmpty() && map.containsKey(name)) {
            val index = map[name]!!.id
            val off: Matrix4f = map[name]!!.offset
            // if (index >= 100) return; // probably not good
            matrices[index] = off.apply { multiply(globalTransform) }
        }
        for (child in node.children) {
            calculateBoneTransformations(child, globalTransform)
        }
    }

    fun getName(): String {
        return animation!!.name
    }
}