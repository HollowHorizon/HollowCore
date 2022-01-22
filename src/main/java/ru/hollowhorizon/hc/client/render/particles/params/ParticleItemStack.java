package ru.hollowhorizon.hc.client.render.particles.params;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public class ParticleItemStack
{
    private final ItemStack value;

    public ParticleItemStack(final ItemStack stack) {
        this.value = stack;
    }

    public ParticleItemStack() {
        this.value = ItemStack.EMPTY;
    }

    public ItemStack value() {
        return this.value;
    }

    public String serialize() {
        return this.value.serializeNBT().toString();
    }

    @Nullable
    public static ParticleItemStack deserialize(final String string) {
        if (string == null) {
            return null;
        }
        return new ParticleItemStack(ItemStack.EMPTY);
    }

    @Nullable
    public static ParticleItemStack deserialize(final PacketBuffer packetBuffer) {
        if (packetBuffer.readBoolean()) {
            return new ParticleItemStack(ItemStack.of(packetBuffer.readNbt()));
        }
        return null;
    }

    public static void serialize(@Nullable final ParticleItemStack inst, final PacketBuffer packetBuffer) {
        if (inst != null && !inst.value.isEmpty()) {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeNbt(inst.value().serializeNBT());
        }
        else {
            packetBuffer.writeBoolean(false);
        }
    }
}
