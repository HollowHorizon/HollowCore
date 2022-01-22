package ru.hollowhorizon.hc.common.handlers;

import net.minecraft.entity.player.ServerPlayerEntity;
import ru.hollowhorizon.hc.common.dialogues.HollowDialogue;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.network.messages.StartDialogueToClient;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DialogueHandler {
    private static final Map<String, HollowDialogue> dialogues = new HashMap<>();

    public static void register(String registryName, HollowDialogue dialogue) {
        dialogues.put(registryName, dialogue);
    }

    public static void start(ServerPlayerEntity player, HollowDialogue dialogue) {
        NetworkHandler.sendMessageToClient(new StartDialogueToClient(dialogue), player);
    }

    public static void start(ServerPlayerEntity player, String dialogue) {
        start(player, get(dialogue));
    }

    public static HollowDialogue get(String registryName) {
        return dialogues.get(registryName);
    }


    public static String getRegName(HollowDialogue dialogue) {
        for (String name : dialogues.keySet()) {
            if (dialogues.get(name).equals(dialogue)) {
                return name;
            }
        }
        return null;
    }

    public static Collection<HollowDialogue> getAll() {
        return dialogues.values();
    }
}
