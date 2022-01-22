package ru.hollowhorizon.hc.client.render.particles.moves;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import ru.hollowhorizon.hc.client.render.particles.base.HollowParticleBase;
import ru.hollowhorizon.hc.client.render.particles.params.IParticleMoveType;

public class ParticleVelocityMover implements IParticleMoveType {
    private Vector3d velocity;
    private boolean decay;

    public ParticleVelocityMover() {
        this.velocity = new Vector3d(0.0D, 0.0D, 0.0D);
        this.decay = false;
    }

    public ParticleVelocityMover(double dx, double dy, double dz, boolean decay) {
        this.velocity = new Vector3d(dx, dy, dz);
        this.decay = decay;
    }

    public void serialize(PacketBuffer buffer) {
        buffer.writeDouble(this.velocity.x);
        buffer.writeDouble(this.velocity.y);
        buffer.writeDouble(this.velocity.z);
        buffer.writeBoolean(this.decay);
    }

    public String serialize() {
        return "VelocityMover:" + this.velocity.x + ":" + this.velocity.y + ":" + this.velocity.z + ":" + this.decay;
    }

    public IParticleMoveType deserialize(PacketBuffer buffer) {
        this.velocity = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        this.decay = buffer.readBoolean();
        return this;
    }

    public void deserialize(String string) {
        if (string.startsWith("VelocityMover")) {
            String[] parts = string.split(":");
            this.velocity = new Vector3d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
            this.decay = Boolean.parseBoolean(parts[4]);
        }

    }

    public void configureParticle(HollowParticleBase particle) {
        particle.setMoveVelocity(this.velocity.x, this.velocity.y, this.velocity.z, this.decay);
    }

    public int getId() {
        return 0;
    }
}

