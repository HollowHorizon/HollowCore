package ru.hollowhorizon.hc.common.commands

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands


class CommandBuilder(private val dispatcher: CommandDispatcher<CommandSourceStack>) {
    operator fun String.invoke(operation: CommandEditor.() -> Unit) {
        val command = Commands.literal(this)
        CommandEditor(command).operation()
        dispatcher.register(command)
    }
}

class CommandEditor(val srcCommand: LiteralArgumentBuilder<CommandSourceStack>) {
    operator fun String.invoke(
        vararg args: RequiredArgumentBuilder<CommandSourceStack, *>,
        operation: CommandContext<CommandSourceStack>.() -> Unit,
    ) {
        if (args.isNotEmpty()) srcCommand.then(
            Commands.literal(this).then(operation, *args)
                .executes { ctx: CommandContext<CommandSourceStack> -> operation(ctx); 1 })
        else srcCommand.then(
            Commands.literal(this).executes { ctx: CommandContext<CommandSourceStack> -> operation(ctx); 1 })
    }
}

fun <S, T : ArgumentBuilder<S, T>> ArgumentBuilder<S, T>.then(
    data: CommandContext<S>.() -> Unit,
    vararg argument: ArgumentBuilder<S, *>,
): T {
    return if (argument.size > 1) this.then(
        argument[0].then(
            data,
            *argument.copyOfRange(1, argument.size)
        )
    ) else this.then(
        argument[0].executes { data(it); 1 } as ArgumentBuilder<S, *>
    )
}

fun arg(name: String, type: ArgumentType<*>) = Commands.argument(name, type)

@JvmName("argString")
fun arg(name: String, type: ArgumentType<*>, suggests: Collection<String>) =
    Commands.argument(name, type).suggests { ctx, builder ->
        suggests.forEach(builder::suggest)
        builder.buildFuture()
    }

@JvmName("argInt")
fun arg(name: String, type: ArgumentType<*>, suggests: Collection<Int>) =
    Commands.argument(name, type).suggests { ctx, builder ->
        suggests.forEach(builder::suggest)
        builder.buildFuture()
    }

fun CommandDispatcher<CommandSourceStack>.register(builder: CommandBuilder.() -> Unit) {
    builder(CommandBuilder(this))
}