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

class HollowCapabilitySerializer<T : Any>(val cap: Capability<T>) : ICapabilitySerializable<INBT> {
    var instance: T = cap.defaultInstance ?: throw NullPointerException("Default instance of capability ${cap.name} is null! May be you forgot to add default constructor?")

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == this.cap) {
            return LazyOptional.of { instance as T }
        }
        return LazyOptional.empty()
    }

    override fun serializeNBT(): INBT {
        return NBTFormat.serializeNoInline(instance, TypeToken.get(instance.javaClass).rawType)
    }


    override fun deserializeNBT(nbt: INBT) {
        instance = NBTFormat.deserializeNoInline(nbt, TypeToken.get(instance.javaClass).rawType) as T
    }

    companion object {
        val ID = CompoundNBT().id
    }
}

fun <T : IHollowCapability> register(clazz: Class<T>) {
    CapabilityManager.INSTANCE.register(
        clazz,
        object : IStorage<T> {
            override fun writeNBT(
                capability: Capability<T>,
                instance: T,
                side: Direction?,
            ): INBT {
                throw UnsupportedOperationException("HollowCapability serialization proceed by custom serializer, look HollowCapabilitySerializer class")
            }

            override fun readNBT(
                capability: Capability<T>,
                instance: T,
                side: Direction?,
                nbt: INBT,
            ) {
                throw UnsupportedOperationException("HollowCapability serialization proceed by custom serializer, look HollowCapabilitySerializer class")
            }
        }
    ) {
        clazz.getConstructor().newInstance()
            ?: throw RuntimeException("Cannot create instance of Capability $clazz, Make default values of parameters")
    }
}

interface IHollowCapability

fun <T: IHollowCapability> T.serialize(): INBT {
    return NBTFormat.serializeNoInline(HollowJavaUtils.castDarkMagic(this), this::class.java)
}

fun deserialize(nbt: INBT): IHollowCapability {
    return NBTFormat.deserialize(nbt)
}

fun <T: IHollowCapability> T.syncClient(playerEntity: PlayerEntity) {
    this.javaClass.createSyncPacket().send(this, playerEntity)
}

fun <T: IHollowCapability> T.syncServer() {
    this.javaClass.createSyncPacket().send(this)
}

fun <T: IHollowCapability> Class<T>.createSyncPacket(): Packet<T> {

    return object : Packet<T>({ player, capability ->
        val cap = HollowCapabilityV2.get(capability.javaClass)

        val capabilityUpdater = player as ICapabilityUpdater

        capabilityUpdater.updateCapability(cap, capability.serialize())
    }, this) {}
}

@HollowCapabilityV2(PlayerEntity::class)
@Serializable
data class MoneyCapability(var money: Int = 0) : IHollowCapability

fun initCapability(cap: Capability<*>, targets: ArrayList<Type>) {
    HollowCapabilityStorageV2.storages[cap.name] = cap

    targets.forEach {
        val clazz = Class.forName(it.className)
        HollowCapabilityStorageV2.providers.add(clazz to HollowCapabilitySerializer(cap))

    }
}
