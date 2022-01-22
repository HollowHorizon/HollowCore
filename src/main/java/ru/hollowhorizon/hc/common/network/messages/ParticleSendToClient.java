package ru.hollowhorizon.hc.common.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import ru.hollowhorizon.hc.common.commands.HollowParticles;

import java.util.function.Supplier;

public class ParticleSendToClient {
    public ParticleSendToClient() {
    }

    public static ParticleSendToClient decode(PacketBuffer buf) {
        return new ParticleSendToClient();
    }

    public static void onReceived(ParticleSendToClient message, Supplier<NetworkEvent.Context> ctxSupplier) {
        ctxSupplier.get().setPacketHandled(true);

        HollowParticles.process(Minecraft.getInstance().player);
    }

    public void encode(PacketBuffer buf) {

    }
}
