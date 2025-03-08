package ru.hollowhorizon.hc.common.objects.molang

import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import org.joml.Vector3f
import org.joml.Vector4f
import ru.hollowhorizon.hc.client.handlers.TickHandler
import ru.hollowhorizon.hc.client.models.internal.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.internal.manager.GltfManager
import ru.hollowhorizon.hc.client.models.internal.manager.IAnimated
import ru.hollowhorizon.hc.client.particles.Transform
import ru.hollowhorizon.hc.common.utils.get
import ru.hollowhorizon.hc.client.utils.math.Quaternion
import ru.hollowhorizon.hc.common.utils.rl
import java.util.*

fun LivingEntity.asMolang() = EntityWrapper(this)

class EntityWrapper(val entity: LivingEntity) : MolangQueryEntity, Transform {
    override val lifeTime: Float
        get() = entity.tickCount + TickHandler.partialTick
    override val modifiedDistanceMoved: Float
        get() = 0f
    override val modifiedMoveSpeed: Float
        get() = entity.attributes.getValue(Attributes.MOVEMENT_SPEED).toFloat()
    override val transform: Transform get() = this
    override val uuid: UUID? get() = entity.uuid
    override val parent: Transform? get() = null
    override val isValid: Boolean get() = entity.isAlive
    override val position: Vector3f
        get() = Vector3f(entity.x.toFloat(), entity.y.toFloat(), entity.z.toFloat())
    override val rotation: Quaternion
        get() = Quaternion.fromAxisAngle(
            Vector3f(0f, 1f, 0f),
            -entity.getViewYRot(TickHandler.partialTick) * Mth.DEG_TO_RAD
        )
    override val velocity: Vector3f
        get() = Vector3f(
            entity.deltaMovement.x.toFloat() * 20f,
            entity.deltaMovement.y.toFloat() * 20f,
            entity.deltaMovement.z.toFloat() * 20f
        )
}

class BoneWrapper<T>(val entity: T, val boneName: String) : Transform where T : LivingEntity, T : IAnimated {
    val model get() = GltfManager.getOrCreate(entity[AnimatedEntityCapability::class].model.rl)

    override val parent: Transform? = null
    override val isValid: Boolean get() = entity.isAlive
    override val position: Vector3f
        get() {
            val pos = Vector4f().mul(model.findPosition(boneName, entity))
            return Vector3f(pos.x, pos.y, pos.z)
        }
    override val rotation: Quaternion
        get() {
            val rot = model.findRotation(boneName)
            return Quaternion(rot.x, rot.y, rot.z, rot.w)
        }
    override val velocity: Vector3f = Vector3f(0f, 0f, 0f)

}