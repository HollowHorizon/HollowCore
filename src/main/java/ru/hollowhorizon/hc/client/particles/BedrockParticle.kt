package ru.hollowhorizon.hc.client.particles

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import dev.folomeev.kotgl.matrix.vectors.*
import dev.folomeev.kotgl.matrix.vectors.mutables.*
import net.minecraft.client.renderer.LightTexture
import net.minecraft.util.Mth
import ru.hollowhorizon.hc.client.molang.MolangContext
import ru.hollowhorizon.hc.client.molang.MolangExpression
import ru.hollowhorizon.hc.client.molang.MolangQuery
import ru.hollowhorizon.hc.client.molang.VariablesMap
import ru.hollowhorizon.hc.client.particles.file.ParticleComponents
import ru.hollowhorizon.hc.client.utils.math.Quaternion
import ru.hollowhorizon.hc.client.utils.math.rotateBy
import ru.hollowhorizon.hc.client.utils.math.rotateSelfBy
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sqrt

class BedrockParticle(
    val emitter: ParticleEmitter,
    val localSpace: Transform?,
) {
    private val components = emitter.effect.components
    private val curveVariables = CurveVariables({ molang }, emitter.effect.curves)
    private val variables = VariablesMap().fallbackBackTo(curveVariables).fallbackBackTo(emitter.context.variables)
    private val molang: MolangContext = MolangContext(MolangQuery.Empty, variables)

    private var firedCreationEvents = false
    private var firedExpirationEvents = false
    private var nextTimelineEvent: Map.Entry<Float, List<String>>? = null

    private var age: Float by variables.getOrPut("particle_age", 0f)
    private var lifetime: Float by variables.getOrPut("particle_lifetime", 0f)

    init {
        for (i in 1..4) variables["particle_random_$i"] = emitter.system.random.nextFloat()

        age = 0f
        lifetime = components.particleLifetimeExpression?.maxLifetime?.eval(molang) ?: 0f
        nextTimelineEvent = components.particleLifetimeEvents.timeline.lowestEntry()
    }

    val position = mutableVec3()
    val velocity = mutableVec3()
    val direction = mutableVec3()

    val globalPosition: Vec3
        get() = if (localSpace != null) position.rotateBy(localSpace.rotation)
            .plusSelf(localSpace.position) else position

    val globalVelocity: Vec3
        get() = if (localSpace != null) velocity.rotateBy(localSpace.rotation) else velocity

    val emitterRotationOnEmit = emitter.rotation

    var rotationAngle = components.particleInitialSpin?.rotation?.eval(molang) ?: 0f
    var rotationRate = components.particleInitialSpin?.rotationRate?.eval(molang) ?: 0f

    var billboardPosition = vec3()
    var billboardRotation = Quaternion.Identity

    var distance: Float = 0f

    fun emit(inheritVelocity: Boolean) {
        var pos: Vec3 = vecZero()
        var dir: Vec3 = vecZero()

        fun ParticleComponents.Direction.computeFor(point: Vec3): Vec3 = when (this) {
            ParticleComponents.Direction.Inwards -> point.times(-1f)
            ParticleComponents.Direction.Outwards -> point
            is ParticleComponents.Direction.Custom -> vec.eval(molang)
        }

        val random = emitter.system.random

        components.emitterShapePoint?.let { config ->
            pos = config.offset.eval(molang)

            val vec = createShape(random)

            vec.normalizeSelf()

            dir = config.direction.computeFor(vec)
        }

        components.emitterShapeBox?.let { config ->
            val point = mutableVec3(
                (random.nextFloat() - 0.5f) * 2f,
                (random.nextFloat() - 0.5f) * 2f,
                (random.nextFloat() - 0.5f) * 2f,
            )

            if (config.surfaceOnly) {
                val side = random.nextInt(5)
                val value = if (side > 2) 1f else -1f
                when (side % 3) {
                    0 -> point.x = value
                    1 -> point.y = value
                    2 -> point.z = value
                }
            }

            point.timesSelf(config.halfDimensions.eval(molang))
            pos = config.offset.eval(molang).plus(point)
            dir = config.direction.computeFor(point)
        }

        components.emitterShapeSphere?.let { config ->
            val vec = createShape(random)

            if (config.surfaceOnly) vec.normalizeSelf()

            vec.timesSelf(config.radius.eval(molang))
            pos = config.offset.eval(molang).plus(vec)
            dir = config.direction.computeFor(vec)
        }

        components.emitterShapeDisc?.let { config ->
            val radius = config.radius.eval(molang)
            val normal = config.planeNormal.eval(molang).normalize()

            val vec = mutableVec3(1f, 0f, 0f)
            if (vec.dot(normal).absoluteValue > 0.9) vec.set(0f, 1f, 0f)

            vec.crossSelf(normal).normalizeSelf()
            vec.rotateSelfBy(Quaternion.fromAxisAngle(normal, random.nextFloat() * 2 * Mth.PI.toFloat()))
            vec.timesSelf(radius * if (config.surfaceOnly) 1f else sqrt(random.nextFloat()))

            pos = config.offset.eval(molang).plus(vec)
            dir = config.direction.computeFor(vec)
        }

        if (components.emitterLocalSpace?.rotation != true) {
            pos = pos.rotateBy(emitter.rotation)
            dir = dir.rotateBy(emitter.rotation)
        }

        if (localSpace == null) {
            pos = pos.plus(emitter.position)
        } else if (emitter.offset != null) {
            pos = pos.plus(emitter.offset)
        }

        position.set(pos)
        direction.set(dir).normalizeSelf()
        velocity.set(direction).timesSelf(components.particleInitialSpeed.eval(molang))

        if (!inheritVelocity && components.emitterLocalSpace?.velocity != true) return

        velocity.plusSelf(emitter.velocity)
    }

    private fun createShape(random: Random) = mutableVec3().apply {
        do {
            set(
                (random.nextFloat() - 0.5f) * 2f,
                (random.nextFloat() - 0.5f) * 2f,
                (random.nextFloat() - 0.5f) * 2f,
            )
        } while (sqrLength().let { it > 1 || it == 0f })
    }

    fun update(dt: Float): Boolean {
        if (!firedCreationEvents) {
            firedCreationEvents = true
            emitter.fire(dt, components.particleLifetimeEvents.creationEvents, this)
        }

        val alive = doUpdate(dt)

        if (!alive && !firedExpirationEvents) {
            firedExpirationEvents = true
            emitter.fire(0f, components.particleLifetimeEvents.expirationEvents, this)
        }

        return alive
    }

    private fun doUpdate(dt: Float): Boolean {
        age += dt

        curveVariables.update()

        fireTimelineEvents()

        if (age >= lifetime) return false

        components.particleLifetimeExpression?.let { config ->
            if (config.expirationExpression.eval(molang) != 0f) return false
        }

        components.particleMotionParametric?.let { config ->
            position.set(config.relativePosition.eval(molang))
            rotationAngle = config.rotation.eval(molang)
            if (config.direction != null) {
                direction.set(config.direction.eval(molang))
                velocity.set(vecZero())
            }
        }

        components.particleMotionDynamic?.let { config ->
            val linearAcceleration = config.linearAcceleration.eval(molang).toMutable()
            linearAcceleration.plusScaledSelf(-config.linearDragCoefficient.eval(molang), velocity)
            if (!move(dt, linearAcceleration)) return false

            var rotAcceleration = config.rotationAcceleration.eval(molang)
            rotAcceleration -= rotationRate * config.rotationDragCoefficient.eval(molang)
            rotAcceleration *= dt
            var deltaRotation = rotationRate
            rotationRate += rotAcceleration
            deltaRotation += rotationRate
            deltaRotation *= 0.5f * dt
            rotationAngle += deltaRotation
        }

        components.particleAppearanceBillboard?.let { config ->
            if (config.direction is ParticleComponents.ParticleBillboard.Direction.FromVelocity) {
                val lengthSqr = velocity.sqrLength()
                if (lengthSqr > config.direction.minSpeedThresholdSqr) direction.set(velocity)
            }
        }

        return true
    }

    private fun fireTimelineEvents() {
        while (true) {
            val (time, events) = nextTimelineEvent ?: return
            val timeSinceEvent = age - time
            if (timeSinceEvent < 0) return

            emitter.fire(timeSinceEvent, events, this)

            nextTimelineEvent = components.particleLifetimeEvents.timeline.higherEntry(time)
        }
    }

    private fun move(dt: Float, acceleration: Vec3, iteration: Int = 0, sliding: Boolean = false): Boolean {
        val offset = mutableVec3(velocity)
        offset.plusScaledSelf(0.5f * dt, acceleration)
        offset.timesSelf(dt)

        val config = components.particleMotionCollision
        if (config == null) {
            position.plusSelf(offset)
            velocity.plusScaledSelf(dt, acceleration)
            return true
        }

        val collision = emitter.system.collisionProvider.query(position, config.collisionRadius, offset)
        if (collision == null) {
            position.plusSelf(offset)
            velocity.plusScaledSelf(dt, acceleration)
            if (sliding) {
                val speedSqr = velocity.sqrLength()
                if (speedSqr > 0.0000001f) {
                    val orgSpeed = sqrt(speedSqr)
                    val modifiedSpeed = (orgSpeed - config.collisionDrag * dt).coerceAtLeast(0f)
                    if (modifiedSpeed > 0.0001f) velocity.timesSelf(modifiedSpeed / orgSpeed)
                    else velocity.set(vecZero())
                } else {
                    velocity.set(vecZero())
                }
            }
            return true
        }

        val (maxOffset, surfaceNormal) = collision

        if (iteration >= 3 || config.expireOnContact) {
            position.plusSelf(maxOffset)
            velocity.plusScaledSelf(dt, acceleration)
            return !config.expireOnContact
        }

        val preDt = sqrt(maxOffset.sqrLength() / offset.sqrLength()).coerceIn(0f, 1f) * dt

        val velocityBeforeHit = velocity.plusScaled(preDt, acceleration)
        val velocityAfterHit = reflect(velocityBeforeHit, surfaceNormal)
        velocityAfterHit.plusScaledSelf(
            (config.coefficientOfRestitution - 1) * velocityAfterHit.dot(surfaceNormal), surfaceNormal
        )
        val positionAtHit = position.plus(maxOffset)

        position.set(positionAtHit)
        velocity.set(velocityAfterHit)

        val postDt = dt - preDt
        val postOffset = mutableVec3(velocityAfterHit)
        postOffset.plusScaledSelf(0.5f * postDt, acceleration)
        postOffset.timesSelf(postDt)
        val positionPostBounce = positionAtHit.plus(postOffset)

        if (config.events.isNotEmpty()) {
            val sqrSpeed = -velocityBeforeHit.dot(surfaceNormal)
            config.events.forEach { eventConfig ->
                if (sqrSpeed >= eventConfig.minSpeed * eventConfig.minSpeed) {
                    emitter.fire(postDt, eventConfig.event, this)
                }
            }
        }

        if (positionPostBounce.dot(surfaceNormal) > positionAtHit.dot(surfaceNormal)) {
            return move(postDt, acceleration, iteration + 1, sliding)
        }

        val accelerationInPlane = acceleration.plusScaled(-acceleration.dot(surfaceNormal), surfaceNormal)
        return move(postDt, accelerationInPlane, iteration + 1, true)
    }

    fun prepareBillboard(cameraPos: Vec3, cameraRot: Quaternion) {
        val appearance = components.particleAppearanceBillboard ?: throw UnsupportedOperationException()
        val position = globalPosition

        fun computeDirection(): Vec3 {
            val localDirection = when (val config = appearance.direction) {
                is ParticleComponents.ParticleBillboard.Direction.FromVelocity -> direction
                is ParticleComponents.ParticleBillboard.Direction.Custom -> config.direction.eval(molang)
            }
            return if (localSpace != null) localDirection.rotateBy(localSpace.rotation)
            else localDirection
        }

        val localSpaceRotation = localSpace?.rotation ?: Quaternion.Identity

        var rot = when (appearance.facingCameraMode) {
            ParticleComponents.ParticleBillboard.FacingCameraMode.ROTATE_XYZ -> cameraRot.opposite()
            ParticleComponents.ParticleBillboard.FacingCameraMode.ROTATE_Y -> cameraRot.opposite()
                .projectAroundAxis(vecUnitY())

            ParticleComponents.ParticleBillboard.FacingCameraMode.LOOK_AT_XYZ -> Quaternion.fromLookAt(
                cameraPos.minus(position), vecUnitY()
            )

            ParticleComponents.ParticleBillboard.FacingCameraMode.LOOK_AT_Y -> Quaternion.fromLookAt(
                cameraPos.minus(position).apply { y = 0f }, vecUnitY()
            )

            ParticleComponents.ParticleBillboard.FacingCameraMode.LOOK_AT_DIRECTION -> {
                val direction = computeDirection()
                val target = cameraPos.minus(position).apply { plusScaledSelf(-this.dot(direction), direction) }
                Quaternion.fromLookAt(target, direction.cross(target).normalizeSelf())
            }

            ParticleComponents.ParticleBillboard.FacingCameraMode.DIRECTION_X -> Quaternion.fromLookAt(
                computeDirection(), vecUnitY().rotateBy(localSpaceRotation)
            ) * Quaternion.fromAxisAngle(vecUnitY(), -Mth.PI / 2)

            ParticleComponents.ParticleBillboard.FacingCameraMode.DIRECTION_Y -> Quaternion.fromLookAt(
                computeDirection(), vecUnitY().rotateBy(localSpaceRotation)
            ) * Quaternion.fromAxisAngle(vecUnitX(), -Mth.PI / 2) * Quaternion.Y180

            ParticleComponents.ParticleBillboard.FacingCameraMode.DIRECTION_Z -> Quaternion.fromLookAt(
                computeDirection(), vecUnitY().rotateBy(localSpaceRotation)
            )

            ParticleComponents.ParticleBillboard.FacingCameraMode.EMITTER_TRANSFORM_XY -> (localSpace?.rotation
                ?: emitterRotationOnEmit) * Quaternion.Y180

            ParticleComponents.ParticleBillboard.FacingCameraMode.EMITTER_TRANSFORM_XZ -> (localSpace?.rotation
                ?: emitterRotationOnEmit) * Quaternion.Y180 * Quaternion.fromAxisAngle(vecUnitX(), Mth.PI / 2)

            ParticleComponents.ParticleBillboard.FacingCameraMode.EMITTER_TRANSFORM_YZ -> (localSpace?.rotation
                ?: emitterRotationOnEmit) * Quaternion.fromAxisAngle(
                vecUnitY(), -Mth.PI / 2
            )
        }

        if (rotationAngle != 0f) {
            rot *= Quaternion.fromAxisAngle(vecUnitZ(), -rotationAngle / 180 * Mth.PI)
        }

        billboardPosition = position
        billboardRotation = rot
    }

    fun renderBillboard(
        matrixStack: PoseStack,
        vertexConsumer: VertexConsumer,
        cameraFacing: Vec3,
        cameraUuid: UUID,
        cameraFirstPerson: Boolean,
    ) {
        if (cameraUuid == emitter.sourceEntity.uuid) {
            if (!components.particleVisibility.let { if (cameraFirstPerson) it.firstPerson else it.thirdPerson }) return
        }

        val appearance = components.particleAppearanceBillboard ?: throw UnsupportedOperationException()

        components.particleInitialization?.perRenderExpression?.eval(molang)

        val position = billboardPosition
        val rotation = billboardRotation
        val (sizeX, sizeY) = appearance.size.eval(molang)
        val textureSize = vec2(appearance.uv.textureWidth.toFloat(), appearance.uv.textureHeight.toFloat())
        val color = components.particleAppearanceTinting?.color?.eval(molang) ?: vec4(1f, 1f, 1f, 1f)
        val light = if (components.particleAppearanceLighting != null) {
            emitter.system.lightProvider.query(position)
        } else {
            LightTexture.FULL_BRIGHT
        }

        var minUV: Vec2
        var maxUV: Vec2

        val flipbook = appearance.uv.flipbook
        if (flipbook != null) {
            val base = flipbook.base.eval(molang)
            val size = flipbook.size.toVec2()
            val step = flipbook.step.toVec2()
            val maxFrame = flipbook.maxFrame.eval(molang).toInt()
            val timePerFrame = if (flipbook.stretchToLifetime) {
                lifetime / maxFrame
            } else {
                1 / flipbook.framePerSecond
            }
            val frame = (age / timePerFrame).toInt().let { frame ->
                if (flipbook.loop) {
                    frame % maxFrame
                } else {
                    frame.coerceAtMost(maxFrame)
                }
            }
            minUV = base.plusScaled(frame.toFloat(), step)
            maxUV = minUV.plus(size)
        } else {
            val base = appearance.uv.uv?.eval(molang) ?: vecZero()
            val size = appearance.uv.uvSize?.eval(molang) ?: textureSize
            minUV = base
            maxUV = minUV.plus(size)
        }

        minUV = minUV.div(textureSize)
        maxUV = maxUV.div(textureSize)

        fun emitPoint(x: Float, y: Float, u: Float, v: Float) {
            val pos = mutableVec3(x, y, 0f)
            pos.rotateSelfBy(rotation)
            vertexConsumer
                .vertex(
                    matrixStack.last().pose(),
                    position.x + pos.x,
                    position.y + pos.y,
                    position.z + pos.z
                )
                .uv(u, v)
                .color(color.x, color.y, color.z, color.w)
                .uv2(light)
                .endVertex()
        }

        val flip =
            if (emitter.effect.material.backfaceCulling) false
            else {
                val billboardNormal = mutableVec3(0f, 0f, -1f).rotateSelfBy(rotation)
                cameraFacing.dot(billboardNormal) > 0
            }

        if (!flip) {
            emitPoint(-sizeX, -sizeY, maxUV.x, maxUV.y)
            emitPoint(-sizeX, +sizeY, maxUV.x, minUV.y)
            emitPoint(+sizeX, +sizeY, minUV.x, minUV.y)
            emitPoint(+sizeX, -sizeY, minUV.x, maxUV.y)
        } else {
            emitPoint(+sizeX, -sizeY, minUV.x, maxUV.y)
            emitPoint(+sizeX, +sizeY, minUV.x, minUV.y)
            emitPoint(-sizeX, +sizeY, maxUV.x, minUV.y)
            emitPoint(-sizeX, -sizeY, maxUV.x, maxUV.y)
        }
    }
}

private fun reflect(vec: Vec3, norm: Vec3) = vec.plusScaled(-2 * vec.dot(norm), norm)

private fun Pair<Float, Float>.toVec2() = mutableVec2(first, second)

private fun Pair<MolangExpression, MolangExpression>.eval(context: MolangContext) =
    mutableVec2(first.eval(context), second.eval(context))