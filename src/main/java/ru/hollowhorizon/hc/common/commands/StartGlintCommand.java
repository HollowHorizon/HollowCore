package ru.hollowhorizon.hc.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;

public class StartGlintCommand {
    public static int execute(CommandContext<CommandSource> context) throws CommandException {
        try {
            int glintColor = IntegerArgumentType.getInteger(context, "color");
            //GlintHandler.startGlint(glintColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
