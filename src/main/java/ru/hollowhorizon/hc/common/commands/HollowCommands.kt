package ru.hollowhorizon.hc.common.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands

object HollowCommands {
    private val commands = ArrayList<Pair<String, Runnable>>()

    @JvmStatic
    fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {
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
