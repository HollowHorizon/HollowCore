package ru.hollowhorizon.hc.common.commands;

import com.mojang.brigadier.Command;
import net.minecraft.command.CommandException;

public class StopGlintCommand {
    public static int execute() throws CommandException {
        try {
            //GlintHandler.stopGlint();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Command.SINGLE_SUCCESS;
    }
}
