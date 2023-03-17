package ru.hollowhorizon.hc.common.objects.entities.animation.layers

import net.minecraft.entity.LivingEntity
import net.minecraft.util.ResourceLocation
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity

class LocomotionLayer<T>(
    name: String, idle: ResourceLocation, run: ResourceLocation, entity: T, shouldLoop: Boolean,
) : BlendTwoPoseLayer<T>(name, idle, run, entity, shouldLoop, 0.0f) where T : LivingEntity, T : IBTAnimatedEntity<T> {
    init {
        computeDuration()
    }

    override var blendAmount: Float
        get() = entity.animationSpeed
        set(blendAmount) {
            super.blendAmount = blendAmount
        }

    private fun computeDuration() {
        val anim = getAnimation(SECOND_SLOT)
        if (anim != null) duration = anim.totalTicks
    }
}