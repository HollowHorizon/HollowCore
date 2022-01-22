package ru.hollowhorizon.hc.client.render.particles.moves;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import ru.hollowhorizon.hc.client.render.particles.base.HollowParticleBase;
import ru.hollowhorizon.hc.client.render.particles.params.IParticleMoveType;

public class ParticleBezierMover implements IParticleMoveType {
    private Vector3d start;
    private Vector3d end;
    private Vector3d controlA;
    private Vector3d controlB;

    public ParticleBezierMover() {
        this.start = new Vector3d(0.0, 0.0, 0.0);
        this.end = new Vector3d(0.0, 0.0, 0.0);
        this.controlA = new Vector3d(0.0, 0.0, 0.0);
        this.controlB = new Vector3d(0.0, 0.0, 0.0);
    }

    public ParticleBezierMover(final Vector3d start, final Vector3d end) {
        this.start = start;
        this.end = end;
    }

    public ParticleBezierMover(final Vector3d start, final Vector3d end, final Vector3d controlA, final Vector3d controlB) {
        this.start = start;
        this.end = end;
        this.controlA = controlA;
        this.controlB = controlB;
    }

    public void serialize(final PacketBuffer buffer) {
        buffer.writeDouble(this.start.x);
        buffer.writeDouble(this.start.y);
        buffer.writeDouble(this.start.z);
        buffer.writeDouble(this.end.x);
        buffer.writeDouble(this.end.y);
        buffer.writeDouble(this.end.z);
        if (this.controlA != null && this.controlB != null) {
            buffer.writeBoolean(true);
            buffer.writeDouble(this.controlA.x);
            buffer.writeDouble(this.controlA.y);
            buffer.writeDouble(this.controlA.z);
            buffer.writeDouble(this.controlB.x);
            buffer.writeDouble(this.controlB.y);
            buffer.writeDouble(this.controlB.z);
        } else {
            buffer.writeBoolean(false);
        }
    }

    public String serialize() {
        if (this.controlA != null && this.controlB != null) {
            return "BezierMover:" + this.start.x + ":" + this.start.y + ":" + this.start.z + ":" + this.end.x + ":" + this.end.y + ":" + this.end.z + ":true:" + this.controlA.x + ":" + this.controlA.y + ":" + this.controlA.z + ":" + this.controlB.x + ":" + this.controlB.y + ":" + this.controlB.z;
        }
        return "BezierMover:" + this.start.x + ":" + this.start.y + ":" + this.start.z + ":" + this.end.x + ":" + this.end.y + ":" + this.end.z + ":false";
    }

    public IParticleMoveType deserialize(final PacketBuffer buffer) {
        this.start = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        this.end = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        if (buffer.readBoolean()) {
            this.controlA = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
            this.controlB = new Vector3d(buffer.readDouble(), buffer.readDouble(), buffer.readDouble());
        }
        return this;
    }

    public void deserialize(final String string) {
        if (string.startsWith("VelocityMover")) {
            final String[] parts = string.split(":");
            this.start = new Vector3d(Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
            this.end = new Vector3d(Double.parseDouble(parts[4]), Double.parseDouble(parts[5]), Double.parseDouble(parts[6]));
            if (Boolean.parseBoolean(parts[7])) {
                this.start = new Vector3d(Double.parseDouble(parts[8]), Double.parseDouble(parts[9]), Double.parseDouble(parts[10]));
                this.end = new Vector3d(Double.parseDouble(parts[11]), Double.parseDouble(parts[12]), Double.parseDouble(parts[13]));
            }
        }
    }

    public void configureParticle(final HollowParticleBase particle) {
        if (this.controlA != null && this.controlB != null) {
            particle.setMoveBezier(this.start, this.end, this.controlA, this.controlB);
        } else {
            particle.setMoveBezier(this.start, this.end);
        }
    }

    public int getId() {
        return 3;
    }
}

