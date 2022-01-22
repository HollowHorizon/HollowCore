package ru.hollowhorizon.hc.client.render.particles.params;

import net.minecraft.network.PacketBuffer;
import ru.hollowhorizon.hc.client.render.particles.base.HollowParticleBase;
import ru.hollowhorizon.hc.client.render.particles.moves.ParticleBezierMover;
import ru.hollowhorizon.hc.client.render.particles.moves.ParticleLerpMover;
import ru.hollowhorizon.hc.client.render.particles.moves.ParticleOrbitMover;
import ru.hollowhorizon.hc.client.render.particles.moves.ParticleVelocityMover;

public interface IParticleMoveType {
    static IParticleMoveType fromID(final int id) {
        switch (id) {
            case 3: {
                return new ParticleBezierMover();
            }
            case 2: {
                return new ParticleOrbitMover();
            }
            case 1: {
                return new ParticleLerpMover();
            }
            default: {
                return new ParticleVelocityMover();
            }
        }
    }

    void serialize(final PacketBuffer p0);

    String serialize();

    IParticleMoveType deserialize(final PacketBuffer p0);

    void deserialize(final String p0);

    void configureParticle(final HollowParticleBase p0);

    int getId();
}
