package ru.hollowhorizon.hc.client.particles

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import dev.folomeev.kotgl.matrix.vectors.*
import dev.folomeev.kotgl.matrix.vectors.mutables.*
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level
import ru.hollowhorizon.hc.client.molang.*
import ru.hollowhorizon.hc.client.particles.collision.CollisionProvider
import ru.hollowhorizon.hc.client.particles.collision.WorldCollisionProvider
import ru.hollowhorizon.hc.client.particles.light.LightProvider
import ru.hollowhorizon.hc.client.particles.light.WorldLightProvider
import ru.hollowhorizon.hc.client.utils.math.*
import ru.hollowhorizon.hc.client.utils.use
import java.util.*


class ParticleSystem(
    val random: Random,
    val collisionProvider: CollisionProvider,
    val lightProvider: LightProvider,
) {
    val timeSource = MolangQueryTime.GLFW_TIME
    var lastUpdate = timeSource.time

    val emitters = mutableListOf<ParticleEmitter>()
    val billboardRenderPasses = mutableMapOf<ParticleEffect.RenderPass, MutableSet<BedrockParticle>>()

    companion object {
        fun create(level: Level) = ParticleSystem(Random(), WorldCollisionProvider(level), WorldLightProvider(level))
    }

    fun remove(name: String) {
        emitters.removeIf { it.effect.identifier == name }
    }

    fun spawn(
        effect: ParticleEffect,
        entity: MolangQueryEntity = MolangQueryEntity.EMPTY,
        transform: Transform = entity.transform,
    ) {
        val emitter =
            ParticleEmitter(this, effect, entity, transform.position, transform.rotation, transform.velocity, transform)
        emitters.add(emitter)

        val dt = (lastUpdate - timeSource.time).coerceAtLeast(0f)
        emitter.startLoop(dt)

        emitter.update(dt)
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
        cameraPos: Vec3,
        cameraRot: Quaternion,
        particleVertexConsumerProvider: VertexConsumerProvider,
        cameraUuid: UUID,
        isFirstPerson: Boolean,
    ) = stack.use {
        translate(-cameraPos.x, -cameraPos.y, -cameraPos.z)

        val cameraFacing = vec3(0f, 0f, -1f).rotateBy(cameraRot)
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
        cameraPos: Vec3,
        cameraRot: Quaternion,
        facing: MutableVec3,
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
        cameraPos: Vec3,
        cameraRot: Quaternion,
    ) {
        particles.forEach { particle ->
            particle.prepareBillboard(cameraPos, cameraRot)

            val billboardNormal = mutableVec3(0f, 0f, -1f).rotateSelfBy(particle.billboardRotation)
            particle.distance = cameraPos.minus(particle.billboardPosition).dot(billboardNormal)
        }
    }

}

