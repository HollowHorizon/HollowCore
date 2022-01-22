package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.client.screens.ProgressManagerScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenSaveGuiMessage {
    List<ITextComponent> list;

    public OpenSaveGuiMessage(List<ITextComponent> list) {
        this.list = list;
    }

    public static OpenSaveGuiMessage decode(PacketBuffer buffer) {
        List<ITextComponent> list = new ArrayList<>();
        list.add(buffer.readComponent());
        list.add(buffer.readComponent());
        list.add(buffer.readComponent());
        list.add(buffer.readComponent());
        list.add(buffer.readComponent());

        return new OpenSaveGuiMessage(list);
    }

    public static void onReceived(OpenSaveGuiMessage message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        Minecraft.getInstance().setScreen(new ProgressManagerScreen(message.list));
    }

    public void encode(PacketBuffer buffer) {
        buffer.writeComponent(this.list.get(0));
        buffer.writeComponent(this.list.get(1));
        buffer.writeComponent(this.list.get(2));
        buffer.writeComponent(this.list.get(3));
        buffer.writeComponent(this.list.get(4));
    }
}
