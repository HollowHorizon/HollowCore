/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.common.commands

import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.client.Minecraft
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.world.entity.LivingEntity
import org.joml.Vector3f
import ru.hollowhorizon.hc.api.ParticlesProvider
import ru.hollowhorizon.hc.client.models.internal.controller.animationController
import ru.hollowhorizon.hc.client.models.internal.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.internal.manager.GltfManager
import ru.hollowhorizon.hc.client.particles.BedrockParticles
import ru.hollowhorizon.hc.client.particles.ParticleEffect
import ru.hollowhorizon.hc.client.particles.Transform
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterCommandsEvent
import ru.hollowhorizon.hc.common.objects.molang.asMolang
import ru.hollowhorizon.hc.common.utils.get
import ru.hollowhorizon.hc.common.utils.rl

object HollowCommands {
    var brightness = 1f

    @SubscribeEvent
    fun onRegisterCommands(event: RegisterCommandsEvent) {
        event.dispatcher.onRegisterCommands {
            "hollowcore" {
                "particle"(
                    arg("pos", Vec3Argument.vec3()),
                    arg(
                        "name",
                        StringArgumentType.greedyString()
                    ) { BedrockParticles.PARTICLES.keys.map { it.toString() } },
                ) {
                    val particle = StringArgumentType.getString(this, "name")
                    val pos = Vec3Argument.getVec3(this, "pos")

                    (Minecraft.getInstance().level as ParticlesProvider).system.spawn(
                        ParticleEffect.fromFile(BedrockParticles.PARTICLES[particle.rl] ?: error("Particle not found")),
                        transform = Transform.create(Vector3f(pos.x.toFloat(), pos.y.toFloat(), pos.z.toFloat())),
                    )
                }

                "particle"(
                    arg("entity", EntityArgument.entity()),
                    arg(
                        "name",
                        StringArgumentType.greedyString()
                    ) { BedrockParticles.PARTICLES.keys.map { it.toString() } },
                ) {
                    val entity = EntityArgument.getEntity(this, "entity")
                    val particle = StringArgumentType.getString(this, "name")

                    (Minecraft.getInstance().level as ParticlesProvider).system.spawn(
                        ParticleEffect.fromFile(BedrockParticles.PARTICLES[particle.rl] ?: error("Particle not found")),
                        entity = (entity as LivingEntity).asMolang(),
                    )
                }

                "remove-particles"(
                    arg(
                        "name",
                        StringArgumentType.greedyString()
                    ) { BedrockParticles.PARTICLES.keys.map { it.toString() } },
                ) {
                    val particle = StringArgumentType.getString(this, "name")
                    val file = BedrockParticles.PARTICLES[particle.rl] ?: error("Particle not found")

                    (Minecraft.getInstance().level as ParticlesProvider).system.remove(
                        file.particleEffect.description.identifier
                    )
                }

                "player-model"(
                    arg("model", StringArgumentType.greedyString()) {
                        GltfManager.allModels.map { it.toString() } + "%NO_MODEL%"
                    }
                ) {
                    source.player?.let {
                        it[AnimatedEntityCapability::class].model = StringArgumentType.getString(this, "model")
                    }
                }

                "player-model-anim"(
                    arg("anim", StringArgumentType.greedyString())
                ) {
                    val name = StringArgumentType.getString(this, "anim")
                    source.player?.let {
                        val controller = it[AnimatedEntityCapability::class].controller
                        it[AnimatedEntityCapability::class].controller = animationController {
                            controller.layers.forEach(::layer)
                            layer("CommandLayer") {
                                stateMachine {
                                    state(name + "_layer") {
                                        clip(name)
                                    }
                                    transition("null", name+"_layer") {
                                        condition("true")
                                        duration(0.25f)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
