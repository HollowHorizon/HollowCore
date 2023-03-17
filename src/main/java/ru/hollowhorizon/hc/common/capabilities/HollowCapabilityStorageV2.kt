package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.event.AttachCapabilitiesEvent
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.toRL
import ru.hollowhorizon.hc.common.network.HollowPacketV2Reg
import kotlin.reflect.KClass

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

    fun <T: IHollowCapability> createPacket(clazz: Class<T>, targets: Array<out KClass<*>>) {
        if(targets.any { it.java.isAssignableFrom(Entity::class.java) }) {
            val packet = clazz.createSyncPacketEntity()

            HollowPacketV2Reg.ENTITY_PACKETS.add(packet)
        } else if (targets.any { it.java.isAssignableFrom(World::class.java)}) {
            val packet = clazz.createSyncPacketClientLevel()

            HollowPacketV2Reg.LEVEL_PACKETS.add(packet)
        } else if (targets.any { it.java.isAssignableFrom(PlayerEntity::class.java)}) {
            val packet = clazz.createSyncPacketPlayer()


            HollowPacketV2Reg.PLAYER_PACKETS.add(packet)
        }
    }

    fun registerAll() {
        capabilities.forEach {
            register(it as Class<IHollowCapability>)
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