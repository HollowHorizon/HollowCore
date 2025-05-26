package ru.hollowhorizon.hc.common.utils.molang

import de.fabmax.kool.math.MutableQuatF
import de.fabmax.kool.math.QuatF
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.math.deg
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.animal.FlyingAnimal
import ru.hollowhorizon.hc.client.handlers.TickHandler
import kotlin.math.abs

class EntityQuery(val entity: LivingEntity) {
    @JvmField val ground_speed = calculateSpeedViaDeltaMovement(entity)
    @JvmField val is_moving = abs(ground_speed) >= MOVEMENT_FACTOR
    @JvmField val is_sneaking = entity.isShiftKeyDown
    @JvmField val is_sprinting = entity.isSprinting
    @JvmField val is_jumping = entity.jumping
    @JvmField val velocity_y = entity.deltaMovement.y
    @JvmField val velocity_x = entity.deltaMovement.x
    @JvmField val velocity_z = entity.deltaMovement.z
    @JvmField val is_flying = entity is FlyingAnimal && entity.isFlying
    @JvmField val fall_ticks = entity.fallFlyingTicks
    @JvmField val is_swimming = entity.isSwimming
    @JvmField val is_sitting = entity.vehicle != null
    @JvmField val is_sleeping = entity.isSleeping
    @JvmField val is_hurt = entity.hurtTime > 0
    @JvmField val is_swinging = entity.swingTime > 0
    @JvmField val is_alive = entity.isAlive
    @JvmField val is_on_ground = entity.onGround()
    @JvmField val head_rot = computeRotation(entity, false, TickHandler.partialTick)
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

private fun computeRotation(
    animatable: LivingEntity,
    switchHeadRot: Boolean,
    partialTick: Float,
): QuatF {

    val bodyYaw = -Mth.rotLerp(partialTick, animatable.yBodyRotO, animatable.yBodyRot)
    val headYaw = -Mth.rotLerp(partialTick, animatable.yHeadRotO, animatable.yHeadRot)
    val netHeadYaw = headYaw - bodyYaw
    val headPitch = -Mth.rotLerp(partialTick, animatable.xRotO, animatable.xRot)

    val xRot: QuatF
    val yRot: QuatF

    if (switchHeadRot) {
        xRot = MutableQuatF().rotate(headPitch.deg, Vec3f.Y_AXIS)
        yRot = MutableQuatF().rotate(netHeadYaw.deg, Vec3f.X_AXIS)
    } else {
        xRot = MutableQuatF().rotate(headPitch.deg, Vec3f.X_AXIS)
        yRot = MutableQuatF().rotate(netHeadYaw.deg, Vec3f.Y_AXIS)
    }

    return yRot.mul(xRot)

}