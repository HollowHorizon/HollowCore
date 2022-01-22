package ru.hollowhorizon.hc.client.render.particles.params;

import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public class ParticleFloat
{
    private final float value;

    public ParticleFloat(final float scale) {
        this.value = scale;
    }

    public float value() {
        return this.value;
    }

    public String serialize() {
        return "" + this.value;
    }

    @Nullable
    public static ParticleFloat deserialize(final String string) {
        if (string == null) {
            return null;
        }
        return new ParticleFloat(Float.parseFloat(string));
    }

    @Nullable
    public static ParticleFloat deserialize(final PacketBuffer packetBuffer) {
        if (packetBuffer.readBoolean()) {
            return new ParticleFloat(packetBuffer.readFloat());
        }
        return null;
    }

    public static void serialize(@Nullable final ParticleFloat inst, final PacketBuffer packetBuffer) {
        if (inst != null) {
            packetBuffer.writeBoolean(true);
            packetBuffer.writeFloat(inst.value());
        }
        else {
            packetBuffer.writeBoolean(false);
        }
    }
}

