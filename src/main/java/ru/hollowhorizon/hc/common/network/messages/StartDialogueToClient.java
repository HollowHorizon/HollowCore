package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.screens.DialogueScreen;
import ru.hollowhorizon.hc.common.handlers.GUIDialogueHandler;
import ru.hollowhorizon.hc.common.story.dialogues.HollowDialogue;

import java.util.Optional;
import java.util.function.Supplier;

public class StartDialogueToClient {

    private boolean messageIsValid;
    private HollowDialogue dialogue;

    public StartDialogueToClient(HollowDialogue dialogue) {
        this.dialogue = dialogue;
        messageIsValid = true;
    }

    public StartDialogueToClient() {
        messageIsValid = false;
    }

    public static StartDialogueToClient decode(PacketBuffer buf) {
        StartDialogueToClient retval = new StartDialogueToClient();
        try {
            String regName = buf.readUtf();

            retval.dialogue = GUIDialogueHandler.get(regName);

        } catch (IllegalArgumentException | IndexOutOfBoundsException e) {
            HollowCore.LOGGER.warn("Exception while reading AirStrikeMessageToServer: " + e);
            return retval;
        }
        retval.messageIsValid = true;
        return retval;
    }

    public static void onReceived(final StartDialogueToClient message, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getDirection().getReceptionSide();
        ctx.setPacketHandled(true);
        if (!sideReceived.isClient()) {
            HollowCore.LOGGER.warn("TargetEffectMessageToClient received on wrong side: server");
            return;
        }

        if (!message.isMessageValid()) {
            HollowCore.LOGGER.warn("TargetEffectMessageToClient was invalid" + message.toString());
            return;
        }

        Optional<ClientWorld> clientWorld = LogicalSidedProvider.CLIENTWORLD.get(sideReceived);
        if (!clientWorld.isPresent()) {
            HollowCore.LOGGER.warn("TargetEffectMessageToClient context could not provide a ClientWorld.");
            return;
        }

        ctx.enqueueWork(() -> processMessage(ctx.getSender(), message));
    }

    private static void processMessage(ServerPlayerEntity player, StartDialogueToClient message) {
        DialogueScreen.openGUI(message.dialogue);
    }

    public void encode(PacketBuffer buf) {
        String regName = GUIDialogueHandler.getRegName(dialogue);

        if (!messageIsValid || regName == null) return;

        buf.writeUtf(regName);
    }

    public boolean isMessageValid() {
        return messageIsValid;
    }


}
