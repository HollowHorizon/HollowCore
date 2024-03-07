package ru.hollowhorizon.hc.common.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.ResourceLocationArgument
import ru.hollowhorizon.hc.client.render.particles.ParticlesExample
import ru.hollowhorizon.hc.client.render.shaders.post.PostChain

object HollowCommands {
    private val commands = ArrayList<Pair<String, Runnable>>()

    @JvmStatic
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register {
            "hollowcore" {
                "particles_example" {
                    ParticlesExample.spawn(source.playerOrException.position())
                }

                "stop-post" {
                    PostChain.shutdown();
                }

                "start-post"(
                    arg("name", ResourceLocationArgument.id())
                ) {
                    val name = ResourceLocationArgument.getId(this, "name")

                    PostChain.apply(name)
                }
            }
        }

        commands.forEach { (name, runnable) ->
            dispatcher.register(Commands.literal(name).executes {
                runnable.run()
                1
            })
        }
    }

    fun addCommand(pair: Pair<String, Runnable>) {
        commands.add(pair)
    }
}
