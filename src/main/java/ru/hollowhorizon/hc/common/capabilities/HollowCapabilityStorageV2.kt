package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.event.AttachCapabilitiesEvent
import ru.hollowhorizon.hc.client.utils.toRL

object HollowCapabilityStorageV2 {
    val capabilities = arrayListOf<Class<*>>()
    val storages = hashMapOf<String, Capability<*>>()
    val providers = arrayListOf<Pair<Class<*>, () -> HollowCapabilitySerializer<*>>>()

    fun getCapabilitiesForClass(clazz: Class<*>): List<Capability<*>> {
        return providers.filter { it.first == clazz }.map { it.second.invoke().cap }
    }

    fun getCapabilityTargets(cap: Capability<*>): List<Class<*>> {
        return providers.filter { it.second.invoke().cap == cap }.map { it.first }
    }

    @Suppress("UNCHECKED_CAST")
    fun registerAll() {
        capabilities.forEach {
            register(it as Class<HollowCapability>)
        }
    }

    @JvmStatic
    fun registerProvidersEntity(event: AttachCapabilitiesEvent<Entity>) {
        providers.filter { it.first.isInstance(event.`object`) }.forEach {
            val inst = it.second()
            event.addCapability(inst.cap.createName(), inst)
        }
    }

    @JvmStatic
    fun registerProvidersWorld(event: AttachCapabilitiesEvent<Level>) {
        providers.filter { it.first.isInstance(event.`object`) }.forEach {
            val inst = it.second()
            event.addCapability(inst.cap.createName(), inst)
        }
    }

    @JvmStatic
    fun registerProvidersTile(event: AttachCapabilitiesEvent<BlockEntity>) {
        providers.filter { it.first.isInstance(event.`object`) }.forEach {
            val inst = it.second()
            event.addCapability(inst.cap.createName(), inst)
        }
    }

    @JvmStatic
    fun registerProvidersChunk(event: AttachCapabilitiesEvent<ChunkAccess>) {
        providers.filter { it.first.isInstance(event.`object`) }.forEach {
            val inst = it.second()
            event.addCapability(inst.cap.createName(), inst)
        }
    }

    private fun Capability<*>.createName(): ResourceLocation {
        return ("hc_capabilities:" +
                this.name.lowercase()
                    .replace(Regex("[^a-z0-9/._-]"), "")
                ).toRL()
    }
}