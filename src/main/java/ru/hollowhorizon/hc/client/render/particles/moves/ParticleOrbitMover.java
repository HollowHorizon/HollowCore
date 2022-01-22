package ru.hollowhorizon.hc.client.render.particles.moves;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import ru.hollowhorizon.hc.client.render.particles.base.HollowParticleBase;
import ru.hollowhorizon.hc.client.render.particles.params.IParticleMoveType;

public class ParticleOrbitMover implements IParticleMoveType {
    private Vector3d center;
    private double forward;
    private double up;
    private double radius;

    public ParticleOrbitMover() {
        this.center = new Vector3d(0.0D, 0.0D, 0.0D);
    }

    public ParticleOrbitMover(double sx, double sy, double sz, double forward, double up, double radius) {
        this.center = new Vector3d(sx, sy, sz);
        this.forward = forward;
        this.up = up;
        this.radius = radius;
    }

    public void serialize(PacketBuffer buffer) {
        buffer.writeDouble(this.center.x);
        buffer.writeDouble(this.center.y);
        buffer.writeDouble(this.center.z);
        buffer.writeDouble(this.forward);
        buffer.writeDouble(this.up);
        buffer.writeDouble(this.radius);
    }

    public String serialize() {
        return "OrbitMover:" + this.center.x + ":" + this.center.y + ":" + this.center.z + ":" + this.forward + ":" + this.up + ":" + this.radius;
    }

    public IParticleMoveType deserialize(PacketBuffer buffer) {
        this.center = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        this.forward = buffer.readDouble();
        this.up = buffer.readDouble();
        this.radius = buffer.readDouble();
        return this;
    }

    public void deserialize(String string) {
        if (string.startsWith("OrbitMover")) {
            String[] parts = string.split(":");
            this.center = new Vector3d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
            this.forward = Double.parseDouble(parts[4]);
            this.up = Double.parseDouble(parts[5]);
            this.radius = Double.parseDouble(parts[6]);
        }

    }

    public void configureParticle(HollowParticleBase particle) {
        particle.setMoveOrbit(this.center.x, this.center.y, this.center.z, this.forward, this.up, this.radius);
    }

    public int getId() {
        return 2;
    }
}
