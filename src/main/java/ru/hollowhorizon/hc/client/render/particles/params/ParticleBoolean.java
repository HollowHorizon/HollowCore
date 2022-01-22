package ru.hollowhorizon.hc.client.render.particles.params;

import net.minecraft.network.PacketBuffer;

import javax.annotation.Nullable;

public class ParticleBoolean
{
    private final boolean value;

    public ParticleBoolean(final boolean life) {
        this.value = life;
    }

    public boolean value() {
        return this.value;
    }

    public String serialize() {
        return "" + this.value;
    }

    public static ParticleBoolean deserialize(final String string) {
        return new ParticleBoolean(Boolean.parseBoolean(string));
    }

    @Nullable
    public static ParticleBoolean deserialize(final PacketBuffer packetBuffer) {
        return new ParticleBoolean(packetBuffer.readBoolean());
    }

    public static void serialize(@Nullable final ParticleBoolean inst, final PacketBuffer packetBuffer) {
        if (inst != null) {
            packetBuffer.writeBoolean(inst.value());
        }
        else {
            packetBuffer.writeBoolean(false);
        }
    }
}
