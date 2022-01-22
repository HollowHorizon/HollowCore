package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.events.OnChoiceComplete;

import java.util.function.Supplier;


public class DialogueChoiceToServer {

    private final String dialogue;

    public DialogueChoiceToServer(String dialogue) {
        this.dialogue = dialogue;
    }

    public static DialogueChoiceToServer decode(PacketBuffer buf) {
        return new DialogueChoiceToServer(buf.readUtf());
    }

    public void encode(PacketBuffer buf) {
        buf.writeUtf(dialogue);
    }

    public static void onReceived(DialogueChoiceToServer message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        MinecraftForge.EVENT_BUS.post(new OnChoiceComplete(message.dialogue, ctxSupplier.get().getSender()));
    }
}