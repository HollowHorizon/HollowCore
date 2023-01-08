package ru.hollowhorizon.hc.common.capabilities

import com.google.gson.reflect.TypeToken
import kotlinx.serialization.Serializable
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.nbt.INBT
import net.minecraft.util.Direction
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.LazyOptional
import org.jetbrains.kotlin.utils.addToStdlib.cast
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils
import ru.hollowhorizon.hc.client.utils.nbt.*
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.Packet
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class HollowCapabilityV2(vararg val value: KClass<*>) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        inline fun <reified T> get(): Capability<T> {
            return HollowCapabilityStorageV2.storages[T::class.java.name] as Capability<T>
        }

        fun <T> get(clazz: Class<T>): Capability<T> {
            return HollowCapabilityStorageV2.storages[clazz.name] as Capability<T>
        }
    }
}

class HollowCapabilitySerializer<T : Any?>(val cap: Capability<T>) : ICapabilitySerializable<INBT> {
    val instance: T? = cap.defaultInstance

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == this.cap) {
            return LazyOptional.of { instance as T }
        }
        return LazyOptional.empty()
    }

    override fun serializeNBT(): INBT =
        cap.writeNBT(instance, null) ?: throw NullPointerException("${cap.name} NBT is null")


    override fun deserializeNBT(nbt: INBT) {
        cap.readNBT(instance, null, nbt)
    }

    companion object {
        val ID = CompoundNBT().id
    }
}

fun <T : HollowCapability<T>> register(clazz: Class<T>) {
    CapabilityManager.INSTANCE.register(
        clazz,
        object : IStorage<T> {
            override fun writeNBT(
                capability: Capability<T>,
                instance: T,
                side: Direction?,
            ): INBT {
                HollowCore.LOGGER.info("value: ${instance}")
                HollowCore.LOGGER.info("instance class: ${instance.javaClass}")
                val typeToken = TypeToken.get(instance.javaClass)
                return NBTFormat.serializeNoInline(instance.value, typeToken.rawType)
            }

            override fun readNBT(
                capability: Capability<T>,
                instance: T,
                side: Direction?,
                nbt: INBT,
            ) {
                val typeToken = TypeToken.get(instance.javaClass)
                val result = NBTFormat.deserializeNoInline(nbt, typeToken.rawType)

                instance.value = result as T
            }
        }
    ) {
        clazz.getConstructor().newInstance()
            ?: throw RuntimeException("Cannot create instance of Capability $clazz, Make default values of parameters")
    }
}

open class HollowCapability<T : HollowCapability<T>> {
    var value: T = this as T
}

fun HollowCapability<*>.syncClient(playerEntity: PlayerEntity) {
    HollowCapabilitySyncPacket().send(this, playerEntity)
}

fun HollowCapability<*>.syncServer() {
    HollowCapabilitySyncPacket().send(this)
}

@HollowPacketV2
class HollowCapabilitySyncPacket: Packet<HollowCapability<*>>({ player, capability ->
    val cap = HollowCapabilityV2.get(capability.javaClass)

    player.getCapability(cap).ifPresent {
        it.value = HollowJavaUtils.castDarkMagic(capability.value)
    }
})

@HollowCapabilityV2(PlayerEntity::class)
@Serializable
data class MoneyCapability(var money: Int = 0) : HollowCapability<MoneyCapability>()

fun initCapability(cap: Capability<*>, targets: ArrayList<Type>) {
    HollowCapabilityStorageV2.storages[cap.name] = cap

    targets.forEach {
        val clazz = Class.forName(it.className)

        HollowCapabilityStorageV2.providers.add(clazz to HollowCapabilitySerializer(cap))

    }
}
