package ru.hollowhorizon.hc.client.models.internal.controller

import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer.Companion.MOVEMENT_FACTOR
import ru.hollowhorizon.hc.common.utils.molang.calculateSpeedViaDeltaMovement
import kotlin.math.abs

private val LivingEntity.animationSpeed: Float
    get() = calculateSpeedViaDeltaMovement(this)

private fun LivingEntity.isMoving() = abs(animationSpeed) >= MOVEMENT_FACTOR