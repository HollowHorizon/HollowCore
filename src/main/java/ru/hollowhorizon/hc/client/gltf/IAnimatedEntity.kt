package ru.hollowhorizon.hc.client.gltf

import net.minecraft.entity.Entity
import net.minecraft.network.datasync.DataParameter
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimation
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimationManager
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimationRaw

interface IAnimatedEntity {
    val animationEntity: Entity
        get() = this as? Entity ?: throw IllegalStateException("IAnimatedModel must be implemented by Entity")
    val ANIMATED_MODEL_DATA: DataParameter<GLTFAnimationManager>

    fun getAnimations(): List<GLTFAnimationRaw> {
        return animationEntity.entityData[ANIMATED_MODEL_DATA].animations
    }

    @OnlyIn(Dist.CLIENT)
    fun processAnimations(renderingAnimations: List<GLTFAnimation>?) {
        val manager = animationEntity.entityData.get(ANIMATED_MODEL_DATA)
        var shouldUpdate = false
        manager.animations.removeIf { animation ->
            val doRemove = renderingAnimations?.find { anim -> anim.name == animation.name }
                ?.update(animation.tick(), animation.loop) ?: false
            shouldUpdate = shouldUpdate || doRemove
            return@removeIf doRemove
        }
        if (shouldUpdate) {
            animationEntity.entityData.set(ANIMATED_MODEL_DATA, manager)
        }
    }

    fun getAnimation(name: String): GLTFAnimationRaw? {
        return getAnimations().firstOrNull { it.name == name }
    }

    fun addAnimation(animation: String) {
        addAnimation(animation, true)
    }

    fun addAnimation(animation: String, loop: Boolean) {
        val manager = animationEntity.entityData.get(ANIMATED_MODEL_DATA)
        manager.addAnimation(animation, loop)
        animationEntity.entityData.set(ANIMATED_MODEL_DATA, manager)
    }

    fun hasAnimation(animation: String): Boolean {
        return animationEntity.entityData.get(ANIMATED_MODEL_DATA).hasAnimation(animation)
    }

    fun stopAnimation(animation: String) {
        val manager = animationEntity.entityData.get(ANIMATED_MODEL_DATA)
        manager.stopAnimation(animation)
        animationEntity.entityData.set(ANIMATED_MODEL_DATA, manager)
    }
}