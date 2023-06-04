package ru.hollowhorizon.hc.client.gltf.animation

import kotlinx.serialization.Serializable

@Serializable
class GLTFAnimationManager {
    val animationQueue = arrayListOf<GLTFAnimationContainer>()

    fun addAnimation(animation: String, playType: PlayType) {
        if (animation.isEmpty() || animation in animationQueue.map { it.name }) return

        animationQueue.add(GLTFAnimationContainer(animation, playType))
    }

    fun stopAnimation(animation: String) {
        animationQueue.removeIf { it.name == animation }
    }

    fun hasAnimation(animation: String): Boolean {
        return animationQueue.any { it.name == animation }
    }

    fun stopAnimations() {
        animationQueue.clear()
    }
}