package ru.hollowhorizon.hc.client.gltf.animation

import kotlinx.serialization.Serializable

@Serializable
class GLTFAnimationManager {
    val animations = arrayListOf<GLTFAnimationRaw>()

    fun addAnimation(animation: String, loop: Boolean) {
        animations.forEach { if (it.name == animation) return }

        animations.add(GLTFAnimationRaw(animation, loop))
    }

    fun hasAnimation(animation: String): Boolean {
        return animations.any { it.name == animation }
    }

    fun stopAnimation(animation: String) {
        animations.removeIf { it.name == animation }
    }

    fun stopAnimations() {
        animations.clear()
    }
}