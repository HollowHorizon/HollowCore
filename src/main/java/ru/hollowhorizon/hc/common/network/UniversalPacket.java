package ru.hollowhorizon.hc.common.network;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.client.utils.HollowNBTSerializer;

public abstract class UniversalPacket<T> {
    public T value;

    @OnlyIn(Dist.CLIENT)
    public void process() {
        onReceived(Minecraft.getInstance().player, value);
    }

    public void process(ServerPlayerEntity player) {
        onReceived(player, value);
    }

    public abstract HollowNBTSerializer<T> serializer();

    public abstract void onReceived(PlayerEntity player, T value);

    public void sendToServer(T value) {
        this.value = value;
        UniversalPacketManager.sendToServer(this);
    }

    public void sendToClient(ServerPlayerEntity playerEntity, T value) {
        this.value = value;
        UniversalPacketManager.sendToClient(playerEntity, this);
    }
}
