package ru.hollowhorizon.hc.common.capabilities

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityProvider
import net.minecraftforge.event.AttachCapabilitiesEvent
import org.jetbrains.kotlin.utils.addToStdlib.UnsafeCastFunction
import org.jetbrains.kotlin.utils.addToStdlib.cast
import ru.hollowhorizon.hc.client.utils.toRL

object CapabilityStorage {
    val storages = hashMapOf<String, Capability<*>>()
    val playerCapabilities = arrayListOf<Capability<*>>()
    val providers = hashSetOf<Pair<Class<*>, (CapabilityProvider<*>) -> CapabilityInstance>>()

    @OptIn(UnsafeCastFunction::class)
    fun getCapabilitiesForPlayer(): List<Capability<CapabilityInstance>> {
        return playerCapabilities.cast()
    }

    @JvmStatic
    fun <T: CapabilityInstance> getCapability(cap: Class<T>): Capability<T> {
        return storages[cap.name.replace(".", "/")] as Capability<T>
    }

    inline fun <reified T: CapabilityInstance> getCapability(): Capability<T> {
        return storages[T::class.java.name] as Capability<T>
    }

    @JvmStatic
    fun registerProvidersEntity(event: AttachCapabilitiesEvent<Entity>) = event.initCapabilities()

    @JvmStatic
    fun registerProvidersBlockEntity(event: AttachCapabilitiesEvent<BlockEntity>) = event.initCapabilities()

    @JvmStatic
    fun registerProvidersWorld(event: AttachCapabilitiesEvent<Level>) = event.initCapabilities()

    private fun <T> AttachCapabilitiesEvent<T>.initCapabilities() {
        providers.filter { it.first.isInstance(this.`object`) }.forEach {
            val inst = it.second(this.`object` as CapabilityProvider<*>)
            this.addCapability(inst.capability.createName(), inst)
        }
    }

    private fun Capability<*>.createName(): ResourceLocation {
        return ("hc_capabilities:" +
                this.name.lowercase()
                    .replace(Regex("[^a-z0-9/._-]"), "")
                ).toRL()
    }
}