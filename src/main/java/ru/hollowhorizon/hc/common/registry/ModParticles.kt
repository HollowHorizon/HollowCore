package ru.hollowhorizon.hc.common.registry

import net.minecraft.client.Minecraft
import net.minecraft.client.particle.SpriteSet
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent
import net.minecraftforge.registries.RegistryObject
import ru.hollowhorizon.hc.client.render.particles.HollowParticleType

object ModParticles : HollowRegistry() {
    val CIRCLE by register("circle", ::HollowParticleType)
    val STAR by register("star", ::HollowParticleType)

    private val GENERATED_LIST = ArrayList<RegistryObject<HollowParticleType>>()

    @JvmStatic
    fun onRegisterParticles(event: ParticleFactoryRegisterEvent) {
        val engine = Minecraft.getInstance().particleEngine
        engine.register(CIRCLE.get()) { set: SpriteSet -> HollowParticleType.Factory(set) }
        engine.register(STAR.get()) { set: SpriteSet -> HollowParticleType.Factory(set) }
        GENERATED_LIST.forEach {
            engine.register(it.get()) { set: SpriteSet -> HollowParticleType.Factory(set) }
        }
    }

    fun addParticle(name: String) {
        val particle by register(name, ::HollowParticleType)
        GENERATED_LIST.add(particle)
    }
}