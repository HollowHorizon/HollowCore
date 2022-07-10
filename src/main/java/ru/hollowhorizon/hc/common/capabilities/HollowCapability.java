package ru.hollowhorizon.hc.common.capabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import ru.hollowhorizon.hc.common.network.HollowPackets;
import ru.hollowhorizon.hc.common.network.UniversalPacket;

public abstract class HollowCapability<T extends HollowCapability<T>> {
    private final ResourceLocation location;

    public HollowCapability(ResourceLocation resourceLocation) {
        HollowCapabilities.CAPABILITIES.put(resourceLocation, (Capability<HollowCapability<?>>) getCapability());
        this.location = resourceLocation;
    }

    public abstract Capability<T> getCapability();

    public abstract CompoundNBT writeNBT();

    public abstract void readNBT(CompoundNBT nbt);

    public void onDeath(PlayerEntity player, PlayerEntity oldPlayer) {
    }

    public ResourceLocation getRegistryName() {
        return location;
    }

    @OnlyIn(Dist.CLIENT)
    public void update() {
        update(Minecraft.getInstance().player);
    }

    public void update(PlayerEntity player) {
        UniversalPacket<HollowCapability<?>> packet = HollowPackets.CAPABILITY_PACKET;

        if (player.level.isClientSide) {
            packet.sendToServer(this);
        } else {
            packet.sendToClient((ServerPlayerEntity) player, this);
        }

    }
}
