package ru.hollowhorizon.hc.common.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.SuggestionProvider
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import kotlin.reflect.KClass

fun buildStringCommand(name: String, action: (CommandSourceStack, String) -> Unit): LiteralArgumentBuilder<CommandSourceStack> {
    return Commands.literal(name).then(Commands.argument("string", StringArgumentType.greedyString()).executes {

        action(it.source, StringArgumentType.getString(it, "string"))
        1
    })
}

class CommandBuilder(private val dispatcher: CommandDispatcher<CommandSourceStack>) {
    operator fun String.invoke(operation: CommandEditor.() -> Unit) {
        val command = Commands.literal(this)
        CommandEditor(command).operation()
        dispatcher.register(command)
    }
}

class CommandEditor(val dispatcher: LiteralArgumentBuilder<CommandSourceStack>) {
    operator fun String.invoke(vararg args: RequiredArgumentBuilder<CommandSourceStack, *>, operation: CommandContext<CommandSourceStack>.() -> Unit) {
        var command = Commands.literal(this)

        args.forEach { arg ->
            command = command.then(arg)
        }

        dispatcher.then(command).executes { ctx ->
            operation(ctx)
            1
        }
    }
}

fun arg(name: String, type: ArgumentType<*>) = Commands.argument(name, type)
@JvmName("argString")
fun arg(name: String, type: ArgumentType<*>, suggests: Collection<String>) = Commands.argument(name, type).suggests { ctx, builder ->
    suggests.forEach(builder::suggest)
    builder.buildFuture()
}
@JvmName("argInt")
fun arg(name: String, type: ArgumentType<*>, suggests: Collection<Int>) = Commands.argument(name, type).suggests { ctx, builder ->
    suggests.forEach(builder::suggest)
    builder.buildFuture()
}

fun CommandDispatcher<CommandSourceStack>.register(builder: CommandBuilder.() -> Unit) {
    builder(CommandBuilder(this))
}