package ru.hollowhorizon.hc.client.particles

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.util.Mth
import org.joml.Vector2f
import org.joml.Vector3f
import org.joml.Vector4f
import ru.hollowhorizon.hc.client.particles.file.ParticleComponents
import ru.hollowhorizon.hc.client.utils.math.Quaternion
import ru.hollowhorizon.hc.client.utils.math.rotateBy
import ru.hollowhorizon.hc.client.utils.math.rotateSelfBy
import ru.hollowhorizon.hc.client.utils.math.times
import ru.hollowhorizon.hc.common.objects.molang.MolangContext
import ru.hollowhorizon.hc.common.objects.molang.MolangExpression
import ru.hollowhorizon.hc.common.objects.molang.MolangQuery
import ru.hollowhorizon.hc.common.objects.molang.VariablesMap
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

    val position = Vector3f()
    val velocity = Vector3f()
    val direction = Vector3f()

    val globalPosition: Vector3f
        get() = if (localSpace != null) position.rotateBy(localSpace.rotation)
            .add(localSpace.position) else position

    val globalVelocity: Vector3f
        get() = if (localSpace != null) velocity.rotateBy(localSpace.rotation) else velocity

    val emitterRotationOnEmit = emitter.rotation

    var rotationAngle = components.particleInitialSpin?.rotation?.eval(molang) ?: 0f
    var rotationRate = components.particleInitialSpin?.rotationRate?.eval(molang) ?: 0f

    var billboardPosition = Vector3f()
    var billboardRotation = Quaternion.Identity

    var distance: Float = 0f

    fun emit(inheritVelocity: Boolean) {
        var pos = Vector3f()
        var dir = Vector3f()

        fun ParticleComponents.Direction.computeFor(point: Vector3f): Vector3f = when (this) {
            ParticleComponents.Direction.Inwards -> point.times(-1f)
            ParticleComponents.Direction.Outwards -> point
            is ParticleComponents.Direction.Custom -> vec.eval(molang)
        }

        val random = emitter.system.random

        components.emitterShapePoint?.let { config ->
            pos = config.offset.eval(molang)

            val vec = createShape(random)

            vec.normalize()

            dir = config.direction.computeFor(vec)
        }

        components.emitterShapeBox?.let { config ->
            val point = Vector3f(
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

            point.mul(config.halfDimensions.eval(molang))
            pos = config.offset.eval(molang).add(point)
            dir = config.direction.computeFor(point)
        }

        components.emitterShapeSphere?.let { config ->
            val vec = createShape(random)

            if (config.surfaceOnly) vec.normalize()

            vec.mul(config.radius.eval(molang))
            pos = config.offset.eval(molang).add(vec)
            dir = config.direction.computeFor(vec)
        }

        components.emitterShapeDisc?.let { config ->
            val radius = config.radius.eval(molang)
            val normal = config.planeNormal.eval(molang).normalize()

            val vec = Vector3f(1f, 0f, 0f)
            if (vec.dot(normal).absoluteValue > 0.9) vec.set(0f, 1f, 0f)

            vec.cross(normal).normalize()
            vec.rotateSelfBy(Quaternion.fromAxisAngle(normal, random.nextFloat() * 2 * Mth.PI.toFloat()))
            vec.mul(radius * if (config.surfaceOnly) 1f else sqrt(random.nextFloat()))

            pos = config.offset.eval(molang).add(vec)
            dir = config.direction.computeFor(vec)
        }

        if (components.emitterLocalSpace?.rotation != true) {
            pos = pos.rotateBy(emitter.rotation)
            dir = dir.rotateBy(emitter.rotation)
        }

        if (localSpace == null) {
            pos = pos.add(emitter.position)
        } else if (emitter.offset != null) {
            pos = pos.add(emitter.offset)
        }

        position.set(pos)
        direction.set(dir).normalize()
        velocity.set(direction).mul(components.particleInitialSpeed.eval(molang))

        if (!inheritVelocity && components.emitterLocalSpace?.velocity != true) return

        velocity.add(emitter.velocity)
    }

    private fun createShape(random: Random) = Vector3f().apply {
        do {
            set(
                (random.nextFloat() - 0.5f) * 2f,
                (random.nextFloat() - 0.5f) * 2f,
                (random.nextFloat() - 0.5f) * 2f,
            )
        } while (lengthSquared().let { it > 1 || it == 0f })
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
                velocity.set(Vector3f())
            }
        }

        components.particleMotionDynamic?.let { config ->
            val linearAcceleration = config.linearAcceleration.eval(molang)
            linearAcceleration.add(Vector3f(velocity).mul(-config.linearDragCoefficient.eval(molang)))
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
                val lengthSqr = velocity.lengthSquared()
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

    private fun move(dt: Float, acceleration: Vector3f, iteration: Int = 0, sliding: Boolean = false): Boolean {
        val offset = Vector3f(velocity)
        offset.add(Vector3f(acceleration).mul(0.5f * dt))
        offset.mul(dt)

        val config = components.particleMotionCollision
        if (config == null) {
            position.add(offset)
            velocity.add(Vector3f(acceleration).mul(dt))
            return true
        }

        val collision = emitter.system.collisionProvider.query(position, config.collisionRadius, offset)
        if (collision == null) {
            position.add(offset)
            velocity.add(Vector3f(acceleration).mul(dt))
            if (sliding) {
                val speedSqr = velocity.lengthSquared()
                if (speedSqr > 0.0000001f) {
                    val orgSpeed = sqrt(speedSqr)
                    val modifiedSpeed = (orgSpeed - config.collisionDrag * dt).coerceAtLeast(0f)
                    if (modifiedSpeed > 0.0001f) velocity.mul(modifiedSpeed / orgSpeed)
                    else velocity.set(Vector3f())
                } else {
                    velocity.set(Vector3f())
                }
            }
            return true
        }

        val (maxOffset, surfaceNormal) = collision

        if (iteration >= 3 || config.expireOnContact) {
            position.add(maxOffset)
            velocity.add(Vector3f(acceleration).mul(dt))
            return !config.expireOnContact
        }

        val preDt = sqrt(maxOffset.lengthSquared() / offset.lengthSquared()).coerceIn(0f, 1f) * dt

        val velocityBeforeHit = velocity.add(Vector3f(acceleration).mul(preDt))
        val velocityAfterHit = reflect(velocityBeforeHit, surfaceNormal)
        velocityAfterHit.add(
            Vector3f(surfaceNormal).mul((config.coefficientOfRestitution - 1) * Vector3f(velocityAfterHit).dot(surfaceNormal))
        )
        val positionAtHit = position.add(maxOffset)

        position.set(positionAtHit)
        velocity.set(velocityAfterHit)

        val postDt = dt - preDt
        val postOffset = Vector3f(velocityAfterHit)
        postOffset.add(Vector3f(acceleration).mul(postDt/2f))
        postOffset.mul(postDt)
        val positionPostBounce = positionAtHit.add(postOffset)

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

        val accelerationInPlane = acceleration.add(surfaceNormal.mul(-Vector3f(acceleration).dot(surfaceNormal)))
        return move(postDt, accelerationInPlane, iteration + 1, true)
    }

    fun prepareBillboard(cameraPos: Vector3f, cameraRot: Quaternion) {
        val appearance = components.particleAppearanceBillboard ?: throw UnsupportedOperationException()
        val position = globalPosition

        fun computeDirection(): Vector3f {
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
                .projectAroundAxis(Vector3f(0f, 1f, 0f))

            ParticleComponents.ParticleBillboard.FacingCameraMode.LOOK_AT_XYZ -> Quaternion.fromLookAt(
                cameraPos.sub(position), Vector3f(0f, 1f, 0f)
            )

            ParticleComponents.ParticleBillboard.FacingCameraMode.LOOK_AT_Y -> Quaternion.fromLookAt(
                cameraPos.sub(position).apply { y = 0f }, Vector3f(0f, 1f, 0f)
            )

            ParticleComponents.ParticleBillboard.FacingCameraMode.LOOK_AT_DIRECTION -> {
                val direction = computeDirection()
                val target = cameraPos.sub(position).apply { add(direction.mul(Vector3f(this).negate().dot(direction))) }
                Quaternion.fromLookAt(target, direction.cross(target).normalize())
            }

            ParticleComponents.ParticleBillboard.FacingCameraMode.DIRECTION_X -> Quaternion.fromLookAt(
                computeDirection(), Vector3f(0f, 1f, 0f).rotateBy(localSpaceRotation)
            ) * Quaternion.fromAxisAngle(Vector3f(0f, 1f, 0f), -Mth.PI / 2)

            ParticleComponents.ParticleBillboard.FacingCameraMode.DIRECTION_Y -> Quaternion.fromLookAt(
                computeDirection(), Vector3f(0f, 1f, 0f).rotateBy(localSpaceRotation)
            ) * Quaternion.fromAxisAngle(Vector3f(1f, 0f, 0f), -Mth.PI / 2) * Quaternion.Y180

            ParticleComponents.ParticleBillboard.FacingCameraMode.DIRECTION_Z -> Quaternion.fromLookAt(
                computeDirection(), Vector3f(0f, 1f, 0f).rotateBy(localSpaceRotation)
            )

            ParticleComponents.ParticleBillboard.FacingCameraMode.EMITTER_TRANSFORM_XY -> (localSpace?.rotation
                ?: emitterRotationOnEmit) * Quaternion.Y180

            ParticleComponents.ParticleBillboard.FacingCameraMode.EMITTER_TRANSFORM_XZ -> (localSpace?.rotation
                ?: emitterRotationOnEmit) * Quaternion.Y180 * Quaternion.fromAxisAngle(Vector3f(1f, 0f, 0f), Mth.PI / 2)

            ParticleComponents.ParticleBillboard.FacingCameraMode.EMITTER_TRANSFORM_YZ -> (localSpace?.rotation
                ?: emitterRotationOnEmit) * Quaternion.fromAxisAngle(
                Vector3f(0f, 1f, 0f), -Mth.PI / 2
            )
        }

        if (rotationAngle != 0f) {
            rot *= Quaternion.fromAxisAngle(Vector3f(0f, 0f, 1f), -rotationAngle / 180 * Mth.PI)
        }

        billboardPosition = position
        billboardRotation = rot
    }

    fun renderBillboard(
        matrixStack: PoseStack,
        vertexConsumer: VertexConsumer,
        cameraFacing: Vector3f,
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
        val size = appearance.size.eval(molang)
        val sizeX = size.x
        val sizeY = size.y
        val textureSize = Vector2f(appearance.uv.textureWidth.toFloat(), appearance.uv.textureHeight.toFloat())
        val color = components.particleAppearanceTinting?.color?.eval(molang) ?: Vector4f(1f, 1f, 1f, 1f)
        val light = if (components.particleAppearanceLighting != null) {
            emitter.system.lightProvider.query(position)
        } else {
            LightTexture.FULL_BRIGHT
        }

        var minUV: Vector2f
        var maxUV: Vector2f

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
            minUV = base.add(Vector2f(step).mul(frame.toFloat()))
            maxUV = Vector2f(minUV).add(size)
        } else {
            val base = appearance.uv.uv?.eval(molang) ?: Vector2f()
            val size = appearance.uv.uvSize?.eval(molang) ?: textureSize
            minUV = base
            maxUV = Vector2f(minUV).add(size)
        }

        minUV = minUV.div(textureSize)
        maxUV = maxUV.div(textureSize)

        fun emitPoint(x: Float, y: Float, u: Float, v: Float) {
            val pos = Vector3f(x, y, 0f)
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
                val billboardNormal = Vector3f(0f, 0f, -1f).rotateSelfBy(rotation)
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

private fun reflect(vec: Vector3f, norm: Vector3f) = vec.add(Vector3f(norm).mul(-2 * Vector3f(vec).dot(norm)))

private fun Pair<Float, Float>.toVec2() = Vector2f(first, second)

private fun Pair<MolangExpression, MolangExpression>.eval(context: MolangContext) =
    Vector2f(first.eval(context), second.eval(context))