package ru.hollowhorizon.hc.client.render.particles.params;

import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public class ParticleInt
{
    private final int value;

    public ParticleInt(final int life) {
        this.value = life;
    }

    public int value() {
        return this.value;
    }

    public String serialize() {
        return "" + this.value;
    }

    @Nullable
    public static ParticleInt deserialize(final String string) {
        if (string == null) {
            return null;
        }
        return new ParticleInt(Integer.parseInt(string));
    }

    @Nullable
    public static ParticleInt deserialize(final PacketBuffer packetBuffer) {
        if (packetBuffer.readBoolean()) {
            return new ParticleInt(packetBuffer.readInt());
        }
        return null;
    }

    public static void serialize(@Nullable final ParticleInt inst, final PacketBuffer packetBuffer) {
        if (inst != null) {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeInt(inst.value());
        }
        else {
            packetBuffer.writeBoolean(false);
        }
    }
}

