package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.entity.Entity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
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
    fun registerProvidersWorld(event: AttachCapabilitiesEvent<World>) {
        providers.filter { it.first.isInstance(event.`object`) }.forEach {
            val inst = it.second()
            event.addCapability(inst.cap.createName(), inst)
        }
    }

    @JvmStatic
    fun registerProvidersTile(event: AttachCapabilitiesEvent<TileEntity>) {
        providers.filter { it.first.isInstance(event.`object`) }.forEach {
            val inst = it.second()
            event.addCapability(inst.cap.createName(), inst)
        }
    }

    @JvmStatic
    fun registerProvidersChunk(event: AttachCapabilitiesEvent<Chunk>) {
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