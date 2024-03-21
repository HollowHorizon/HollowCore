package ru.hollowhorizon.hc.particles.client

import ru.hollowhorizon.hc.particles.EffekseerParticles
import ru.hollowhorizon.hc.particles.api.common.ParticleHelper.addParticle
import ru.hollowhorizon.hc.particles.api.common.ParticleEmitterInfo
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.LightningBolt

object ModClientHooks {
    val LIGHTNING_EFFEK_TEMPLATE: ParticleEmitterInfo = ParticleEmitterInfo(EffekseerParticles.loc("lightning"))

    @JvmStatic
    fun playLightningEffek(bolt: LightningBolt) {
        val info =
            ParticleEmitterInfo(EffekseerParticles.loc("fire")).bindOnEntity(Minecraft.getInstance().player!!)
        addParticle(bolt.level, true, info)

        Thread {
            try {
                Thread.sleep(1000)
                playLightningEffek(bolt)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()
    }
}
