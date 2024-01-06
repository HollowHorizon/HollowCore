package ru.hollowhorizon.hc.client.render.particles

import net.minecraft.world.phys.Vec3
import net.minecraftforge.server.ServerLifecycleHooks
import ru.hollowhorizon.hc.client.utils.math.Interpolation

object ParticlesExample {
    fun spawn(pos: Vec3) {
        val builder =
            HollowParticleBuilder.create(ServerLifecycleHooks.getCurrentServer().overworld(), "hc:hollow_particle") {
                transparency(0f, 1f, 0f)
                spin(0.1f, 0.4f, 0f, 1.0f, Interpolation.QUINT_OUT, Interpolation.SINE_IN)
                scale(0.15f, 0.4f, 0.35f, 1.0f, Interpolation.QUINT_OUT, Interpolation.SINE_IN)
                color(0.86f, 0.78f, 0.66f, 0.88f, 0.61f, 0.22f)
                randomOffset(0.05, 0.05)
                randomMotion(0.005)

                lifetime = 250
                gravity = 0.05f
            }

        for (i in 0..10) {
            builder.repeatCircle(
                pos.x,
                pos.y + 0.5 * i + 0.1,
                pos.z,
                5.0 / (i + 1),
                100
            )
        }
    }
}