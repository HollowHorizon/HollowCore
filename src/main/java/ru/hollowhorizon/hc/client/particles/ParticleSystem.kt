package ru.hollowhorizon.hc.client.particles

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.world.level.Level
import org.joml.Matrix4f
import org.joml.Vector3f
import ru.hollowhorizon.hc.common.objects.molang.MolangQueryEntity
import ru.hollowhorizon.hc.common.objects.molang.MolangQueryTime
import ru.hollowhorizon.hc.client.particles.collision.CollisionProvider
import ru.hollowhorizon.hc.client.particles.collision.WorldCollisionProvider
import ru.hollowhorizon.hc.client.particles.file.BedrockParticleFile
import ru.hollowhorizon.hc.client.particles.light.LightProvider
import ru.hollowhorizon.hc.client.particles.light.WorldLightProvider
import ru.hollowhorizon.hc.client.utils.math.Quaternion
import ru.hollowhorizon.hc.client.utils.math.rotateBy
import ru.hollowhorizon.hc.client.utils.math.rotateSelfBy
import ru.hollowhorizon.hc.client.utils.use
import java.util.*

class ParticleSystem(
    val random: Random,
    val collisionProvider: CollisionProvider,
    val lightProvider: LightProvider,
) {
    private val timeSource = MolangQueryTime.GLFW_TIME
    private var lastUpdate = timeSource.time

    internal val emitters = mutableListOf<ParticleEmitter>()
    val billboardRenderPasses = mutableMapOf<ParticleEffect.RenderPass, MutableSet<BedrockParticle>>()

    companion object {
        fun create(level: Level) = ParticleSystem(Random(), WorldCollisionProvider(level), WorldLightProvider(level))
    }

    fun remove(name: String) {
        emitters.removeIf { emitter ->
            val shouldRemove = emitter.effect.identifier == name
            if (shouldRemove) emitter.particles.forEach { emitter.onRemove(it) }
            shouldRemove
        }
    }

    fun remove(emitter: ParticleEmitter) {
        emitter.particles.forEach { emitter.onRemove(it) }
        emitters.remove(emitter)
    }

    fun spawn(
        effect: BedrockParticleFile,
        entity: MolangQueryEntity = MolangQueryEntity.EMPTY,
        transform: Transform = entity.transform,
    ) = spawn(ParticleEffect.fromFile(effect), entity, transform)

    fun spawn(
        effect: ParticleEffect,
        entity: MolangQueryEntity = MolangQueryEntity.EMPTY,
        transform: Transform = entity.transform,
    ): ParticleEmitter {
        val emitter =
            ParticleEmitter(this, effect, entity, transform.position, transform.rotation, transform.velocity, transform)
        emitters.add(emitter)

        val dt = (lastUpdate - timeSource.time).coerceAtLeast(0f)
        emitter.startLoop(dt)

        emitter.update(dt)

        return emitter
    }

    fun update() {
        val now = timeSource.time
        val dt = now - lastUpdate
        lastUpdate = now

        emitters.removeIf { !it.update(dt) }
    }

    fun isEmpty() = emitters.isEmpty()
    fun hasAnythingToRender() = billboardRenderPasses.isNotEmpty()

    fun render(
        stack: PoseStack,
        cameraPos: Vector3f,
        cameraRot: Quaternion,
        particleVertexConsumerProvider: VertexConsumerProvider,
        cameraUuid: UUID,
        isFirstPerson: Boolean,
    ) = stack.use {
        mulPoseMatrix(Matrix4f().translate(-cameraPos.x, -cameraPos.y, -cameraPos.z))
        //translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)

        val cameraFacing = Vector3f(0f, 0f, -1f).rotateBy(cameraRot)
        for ((renderPass, particles) in billboardRenderPasses.entries.sortedBy { it.key.material.needsSorting }) {
            particleVertexConsumerProvider.provide(renderPass) { vertexConsumer ->
                drawParticles(
                    particles,
                    this,
                    vertexConsumer,
                    cameraPos,
                    cameraRot,
                    cameraFacing,
                    cameraUuid,
                    isFirstPerson,
                    renderPass.material.needsSorting
                )
            }
        }
    }


    private fun drawParticles(
        particles: MutableSet<BedrockParticle>,
        stack: PoseStack,
        vertexConsumer: VertexConsumer,
        cameraPos: Vector3f,
        cameraRot: Quaternion,
        facing: Vector3f,
        uuid: UUID,
        isFirstPersion: Boolean,
        sort: Boolean = false,
    ) {
        if (sort) calculateDistance(particles, cameraPos, cameraRot)

        particles.sortedByDescending { it.distance }.forEach { particle ->
            if (!sort) particle.prepareBillboard(cameraPos, cameraRot)
            particle.renderBillboard(stack, vertexConsumer, facing, uuid, isFirstPersion)
        }
    }

    private fun calculateDistance(
        particles: MutableSet<BedrockParticle>,
        cameraPos: Vector3f,
        cameraRot: Quaternion,
    ) {
        particles.forEach { particle ->
            particle.prepareBillboard(cameraPos, cameraRot)

            val billboardNormal = Vector3f(0f, 0f, -1f).rotateSelfBy(particle.billboardRotation)
            particle.distance = cameraPos.sub(particle.billboardPosition).dot(billboardNormal)
        }
    }

}

