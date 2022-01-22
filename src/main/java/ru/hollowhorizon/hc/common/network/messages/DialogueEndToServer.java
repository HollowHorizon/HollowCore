package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.events.OnDialogueEndEvent;

import java.util.function.Supplier;


public class DialogueEndToServer {

    private final String dialogue;

    public DialogueEndToServer(String dialogue) {
        this.dialogue = dialogue;
    }

    public static DialogueEndToServer decode(PacketBuffer buf) {
        return new DialogueEndToServer(buf.readUtf());
    }

    public void encode(PacketBuffer buf) {
        buf.writeUtf(dialogue);
    }

    public static void onReceived(DialogueEndToServer message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        MinecraftForge.EVENT_BUS.post(new OnDialogueEndEvent(message.dialogue, ctxSupplier.get().getSender()));
    }
}
