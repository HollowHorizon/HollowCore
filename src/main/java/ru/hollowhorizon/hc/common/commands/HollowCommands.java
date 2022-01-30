package ru.hollowhorizon.hc.common.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import ru.hollowhorizon.hc.common.story.dialogues.HollowDialogue;
import ru.hollowhorizon.hc.common.handlers.GUIDialogueHandler;
import ru.hollowhorizon.hc.common.handlers.InGameDialogueHandler;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.network.messages.ParticleSendToClient;
import ru.hollowhorizon.hc.common.story.events.StoryEventListener;
import ru.hollowhorizon.hc.common.story.events.StoryEventStarter;

import java.util.Objects;


public class HollowCommands {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LiteralArgumentBuilder<CommandSource> lore = Commands.literal("start_event");
        LiteralArgumentBuilder<CommandSource> dialogues = Commands.literal("start_dialogue");

        for (String s : StoryEventListener.getAll()) {
            lore.then(Commands.literal(s).executes((command) -> {
                StoryEventStarter.start(command.getSource().getPlayerOrException(), s);
                return Command.SINGLE_SUCCESS;
            }).then(Commands.argument("player", EntityArgument.players()).executes((command) -> {
                for (ServerPlayerEntity p : EntityArgument.getPlayers(command, "player")) {
                    StoryEventStarter.start(p, s);
                }
                return Command.SINGLE_SUCCESS;
            })));
        }

        for (HollowDialogue dialogue : GUIDialogueHandler.getAll()) {
            dialogues.then(Commands.literal(Objects.requireNonNull(GUIDialogueHandler.getRegName(dialogue))).executes((command) -> {
                GUIDialogueHandler.start(command.getSource().getPlayerOrException(), dialogue);
                return Command.SINGLE_SUCCESS;
            }).then(Commands.argument("player", EntityArgument.players()).executes((command) -> {
                for (ServerPlayerEntity p : EntityArgument.getPlayers(command, "player")) {
                    GUIDialogueHandler.start(p, dialogue);
                }
                return Command.SINGLE_SUCCESS;
            }).then(Commands.argument("isWindow", BoolArgumentType.bool()).executes((command) -> {
                for (ServerPlayerEntity p : EntityArgument.getPlayers(command, "player")) {
                    if (BoolArgumentType.getBool(command, "isWindow")) {
                        GUIDialogueHandler.start(p, dialogue);
                    } else {
                        InGameDialogueHandler.start(p, dialogue);
                    }
                }
                return Command.SINGLE_SUCCESS;

            }))));
        }

        LiteralArgumentBuilder<CommandSource> commandBuilder = Commands.literal("hollow-core")
                .then(lore)
                .then(dialogues)
                .then(Commands.literal("test").executes((source) -> {
                    NetworkHandler.sendMessageToClient(new ParticleSendToClient(), source.getSource().getPlayerOrException());
                            return 1;
                        })
                )
                .then(Commands.literal("utils")
                        .then(Commands.literal("glint")
                                .then(Commands.literal("start")
                                        .then(Commands.argument("color", IntegerArgumentType.integer(0, 16))
                                                .executes(StartGlintCommand::execute)))
                                .then(Commands.literal("stop")
                                        .executes(context -> StopGlintCommand.execute())
                                ))
                );


        dispatcher.register(commandBuilder);
    }
}
