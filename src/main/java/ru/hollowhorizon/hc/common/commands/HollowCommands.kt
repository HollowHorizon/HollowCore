package ru.hollowhorizon.hc.common.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import ru.hollowhorizon.hc.client.render.particles.ParticlesExample

object HollowCommands {
    private val commands = ArrayList<Pair<String, Runnable>>()

    @JvmStatic
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
        dispatcher.register {
            "hollowcore" {
                "particles_example" {
                    ParticlesExample.spawn(source.playerOrException.position())
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
