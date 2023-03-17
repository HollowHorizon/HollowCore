package ru.hollowhorizon.hc.common.objects.entities.animation.layers

import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.models.core.animation.BakedAnimation
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer.AnimationLayerMessage
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.layer.ChangeLayerAnimationMessage

abstract class LayerWithAnimation<T>(name: String, animName: ResourceLocation, entity: T) :
    AnimationLayerBase<T>(name, entity) where T : Entity, T : IBTAnimatedEntity<T> {
    private val slots = HashMap<String, BakedAnimation>()

    init {
        setAnimation(animName, BASE_SLOT)
        addMessageCallback(ChangeLayerAnimationMessage.CHANGE_ANIMATION_TYPE) { message: AnimationLayerMessage ->
            consumeChangeAnimation(message)
        }
        computeDuration()
    }

    private fun computeDuration() {
        val anim: BakedAnimation? = getAnimation(BASE_SLOT)
        if (anim != null) {
            duration = anim.totalTicks
        }
    }

    fun setAnimation(anim: ResourceLocation, slotName: String) {
        val skeleton = entity.skeleton

        val animation: BakedAnimation = skeleton.getBakedAnimation(anim) ?: run {
            HollowCore.LOGGER.error("Animation {} not found for entity: {}", anim.toString(), entity.name.string)
            return
        }
        slots[slotName] = animation

    }

    protected fun changeAnimationHandler(message: ChangeLayerAnimationMessage) {
        setAnimation(message.anim, message.slot)
    }

    private fun consumeChangeAnimation(message: AnimationLayerMessage) {
        if (message is ChangeLayerAnimationMessage) {
            changeAnimationHandler(message)
        }
    }

    fun getAnimation(slotName: String): BakedAnimation? {
        return slots[slotName]
    }

    companion object {
        const val BASE_SLOT = "BASE"
    }
}
