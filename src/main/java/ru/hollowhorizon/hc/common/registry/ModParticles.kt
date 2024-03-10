package ru.hollowhorizon.hc.common.registry

import net.minecraft.client.particle.SpriteSet
import net.minecraftforge.client.event.RegisterParticleProvidersEvent
import net.minecraftforge.registries.RegistryObject
import ru.hollowhorizon.hc.client.render.particles.HollowParticleType

object ModParticles : HollowRegistry() {
    private val CIRCLE by register("circle", ::HollowParticleType)
    private val STAR by register("star", ::HollowParticleType)

    private val GENERATED_LIST = ArrayList<RegistryObject<HollowParticleType>>()

    @JvmStatic
    fun onRegisterParticles(event: RegisterParticleProvidersEvent) {
        event.register(CIRCLE.get()) { set: SpriteSet -> HollowParticleType.Factory(set) }
        event.register(STAR.get()) { set: SpriteSet -> HollowParticleType.Factory(set) }
        GENERATED_LIST.forEach {
            event.register(it.get()) { set: SpriteSet -> HollowParticleType.Factory(set) }
        }
    }

    fun addParticle(name: String) {
        val particle by register(name, ::HollowParticleType)
        GENERATED_LIST.add(particle)
    }
}