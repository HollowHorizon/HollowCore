package ru.hollowhorizon.hc.client.gltf.animation

import kotlinx.serialization.Serializable

@Serializable
class GLTFAnimationManager {
    val animations = arrayListOf<GLTFAnimationContainer>()
    val markedToRemove = hashSetOf<GLTFAnimationContainer>()

    //Устанавливает только ОДНУ, текущую анимацию
    fun setAnimation(animation: String, loop: Boolean = false) {
        if(animation.isEmpty()) return
        if (animations.size > 1) animations.removeIf {it.loop}

        if(animations.firstOrNull()?.name == animation) return

        animations.add(GLTFAnimationContainer(animation, loop))
    }

    //Возвращает — получилось ли добавить анимацию
    fun addAnimation(animation: String, loop: Boolean = false): Boolean {
        if(animation.isEmpty()) return false

        animations.forEach { if (it.name == animation) return false }

        animations.add(GLTFAnimationContainer(animation, loop))

        return true
    }

    fun hasAnimation(animation: String): Boolean {
        return animations.any { it.name == animation }
    }

    fun stopAnimation(animation: String) {
        markedToRemove.addAll(animations.filter { it.name == animation })
    }

    fun stopAnimations() {
        animations.clear()
    }
}