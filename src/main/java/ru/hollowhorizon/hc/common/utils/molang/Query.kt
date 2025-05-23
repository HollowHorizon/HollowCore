package ru.hollowhorizon.hc.common.utils.molang

import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.client.handlers.TickHandler
import ru.hollowhorizon.hc.client.models.internal.manager.HeadLayer
import kotlin.math.abs

class EntityQuery(val entity: LivingEntity) {
    @JvmField val ground_speed = calculateSpeedViaDeltaMovement(entity)
    @JvmField val is_moving = abs(ground_speed) >= MOVEMENT_FACTOR
    @JvmField val is_sneaking = entity.isShiftKeyDown
    @JvmField val head_rot = HeadLayer.computeRotation(entity, false, TickHandler.partialTick)
}

private const val MOVEMENT_FACTOR = (1 / 256f)

fun calculateSpeedViaDeltaMovement(entity: LivingEntity): Float {
    // 1) берём горизонтальную часть вектора скорости (блоки/тик)
    val vel = entity.deltaMovement
    val dx = vel.x.toFloat()
    val dz = vel.z.toFloat()

    // 2) вектор «вперед» по ориентации тела
    val yawRad = Math.toRadians(entity.yBodyRot.toDouble()).toFloat()
    val forwardX = -Mth.sin(yawRad)
    val forwardZ = Mth.cos(yawRad)

    // 3) проекция вектора скорости на вектор «вперед» (чтобы знать направленную скорость)
    val dot = dx * forwardX + dz * forwardZ

    // 4) переводим блоки/тик → блоки/сек
    return dot * 20f
}