package ru.hollowhorizon.hc.common.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.ResourceLocationArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import ru.hollowhorizon.hc.client.render.particles.ParticlesExample
import ru.hollowhorizon.hc.client.render.shaders.post.PostChain
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.particles.api.common.ParticleEmitterInfo
import ru.hollowhorizon.hc.particles.api.common.ParticleHelper

object HollowCommands {

    @JvmStatic
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register {
            "hollowcore" {
                "particles_example" {
                    ParticlesExample.spawn(source.playerOrException.position())
                }

                "stop-post" {
                    PostChain.shutdown()
                }

                "start-post"(
                    arg("name", ResourceLocationArgument.id())
                ) {
                    val name = ResourceLocationArgument.getId(this, "name")

                    PostChain.apply(name)
                }

                "particle"(
                    arg("pos", Vec3Argument.vec3()),
                    arg("name", StringArgumentType.greedyString())
                ) {
                    val particle = StringArgumentType.getString(this, "name")
                    val pos = Vec3Argument.getVec3(this, "pos")

                    val info =
                        ParticleEmitterInfo(particle.removeSuffix(".efkefc").rl).position(pos)
                    ParticleHelper.addParticle(source.level, true, info)

                }

                "particle"(
                    arg("entities", EntityArgument.entities()),
                    arg("target", StringArgumentType.word()),
                    arg("name", StringArgumentType.greedyString())
                ) {
                    val particle = StringArgumentType.getString(this, "name")
                    val target = StringArgumentType.getString(this, "target")
                    val entities = EntityArgument.getEntities(this, "entities")
                    entities.forEach {
                        val info =
                            ParticleEmitterInfo(particle.removeSuffix(".efkefc").rl)
                                .bindOnEntity(it)
                                .apply { bindOnTarget(target) }
                        ParticleHelper.addParticle(source.level, true, info)
                    }



                }
            }
        }
    }
}
