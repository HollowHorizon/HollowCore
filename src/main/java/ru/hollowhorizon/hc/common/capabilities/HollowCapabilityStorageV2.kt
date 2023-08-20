package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.event.AttachCapabilitiesEvent
import ru.hollowhorizon.hc.client.utils.toRL

object HollowCapabilityStorageV2 {
    val capabilities = hashSetOf<Class<*>>()
    val storages = hashMapOf<String, Capability<*>>()
    val providers = hashSetOf<Pair<Class<*>, () -> HollowCapabilitySerializer<*>>>()

    fun getCapabilitiesForClass(clazz: Class<*>): List<Capability<*>> {
        return providers.filter { it.first == clazz }.map { it.second.invoke().capability }
    }

    fun getCapabilityTargets(cap: Capability<*>): List<Class<*>> {
        return providers.filter { it.second.invoke().capability == cap }.map { it.first }
    }

    @JvmStatic
    fun registerProvidersEntity(event: AttachCapabilitiesEvent<Entity>) = event.initCapabilities()

    @JvmStatic
    fun registerProvidersBlockEntity(event: AttachCapabilitiesEvent<BlockEntity>) = event.initCapabilities()

    @JvmStatic
    fun registerProvidersWorld(event: AttachCapabilitiesEvent<Level>) = event.initCapabilities()

    private fun <T> AttachCapabilitiesEvent<T>.initCapabilities() {
        providers.filter { it.first.isInstance(this.`object`) }.forEach {
            val inst = it.second()
            this.addCapability(inst.capability.createName(), inst)
            this.addListener(inst::invalidate)
        }
    }

    private fun Capability<*>.createName(): ResourceLocation {
        return ("hc_capabilities:" +
                this.name.lowercase()
                    .replace(Regex("[^a-z0-9/._-]"), "")
                ).toRL()
    }
}