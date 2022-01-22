package ru.hollowhorizon.hc.client.render.particles.moves;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import ru.hollowhorizon.hc.client.render.particles.base.HollowParticleBase;
import ru.hollowhorizon.hc.client.render.particles.params.IParticleMoveType;

public class ParticleLerpMover implements IParticleMoveType {
    private Vector3d start;
    private Vector3d end;

    public ParticleLerpMover() {
        this.start = new Vector3d(0.0D, 0.0D, 0.0D);
        this.end = new Vector3d(0.0D, 0.0D, 0.0D);
    }

    public ParticleLerpMover(double sx, double sy, double sz, double ex, double ey, double ez) {
        this.start = new Vector3d(sx, sy, sz);
        this.end = new Vector3d(ex, ey, ez);
    }

    public void serialize(PacketBuffer buffer) {
        buffer.writeDouble(this.start.x);
        buffer.writeDouble(this.start.y);
        buffer.writeDouble(this.start.z);
        buffer.writeDouble(this.end.x);
        buffer.writeDouble(this.end.y);
        buffer.writeDouble(this.end.z);
    }

    public String serialize() {
        return "LerpMover:" + this.start.x + ":" + this.start.y + ":" + this.start.z + ":" + this.end.x + ":" + this.end.y + ":" + this.end.z;
    }

    public IParticleMoveType deserialize(PacketBuffer buffer) {
        this.start = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        this.end = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        return this;
    }

    public void deserialize(String string) {
        if (string.startsWith("VelocityMover")) {
            String[] parts = string.split(":");
            this.start = new Vector3d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
            this.end = new Vector3d(Double.parseDouble(parts[4]), Double.parseDouble(parts[5]), Double.parseDouble(parts[6]));
        }

    }

    public void configureParticle(HollowParticleBase particle) {
        particle.setMoveLerp(this.start.x, this.start.y, this.start.z, this.end.x, this.end.y, this.end.z);
    }

    public int getId() {
        return 1;
    }
}
