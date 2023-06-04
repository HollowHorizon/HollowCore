package ru.hollowhorizon.hc.common.capabilities

import com.google.common.reflect.TypeToken
import kotlinx.serialization.Serializable
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.entity.player.ServerPlayerEntity
import net.minecraft.nbt.INBT
import net.minecraft.util.Direction
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.Capability.IStorage
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.CapabilityProvider
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fml.network.PacketDistributor
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserialize
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline
import ru.hollowhorizon.hc.common.network.Packet
import ru.hollowhorizon.hc.common.network.messages.CapabilityForEntity
import ru.hollowhorizon.hc.common.network.messages.SyncCapabilityEntity
import ru.hollowhorizon.hc.common.network.messages.SyncCapabilityPlayer
import ru.hollowhorizon.hc.common.network.messages.SyncCapabilityWorld
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
annotation class HollowCapabilityV2(vararg val value: KClass<*>) {
    @Suppress("UNCHECKED_CAST")
    companion object {
        inline fun <reified T> get(): Capability<T> {
            return HollowCapabilityStorageV2.storages[T::class.java.name] as Capability<T>
        }

        fun <T> get(clazz: Class<T>): Capability<T> {
            return HollowCapabilityStorageV2.storages[clazz.name] as Capability<T>
        }
    }
}

inline fun <reified T> CapabilityProvider<*>.getCapability(): T {
    return this.getCapability(HollowCapabilityV2.get<T>())
        .orElseThrow { IllegalStateException("Capability ${T::class.java.simpleName} not found!") }
}

inline fun <reified T : HollowCapability> CapabilityProvider<*>.useCapability(
    update: Boolean = true,
    crossinline task: T.() -> Unit
) {
    this.getCapability(HollowCapabilityV2.get<T>()).ifPresent {
        task(it)
        if (update) {
            when (this) {
                is PlayerEntity -> it.syncClient(this)
                is Entity -> it.syncEntity(this)
                is World -> it.syncWorld(*this.players().toTypedArray())
            }
        }
    }
}

@Suppress("UnstableApiUsage")
class HollowCapabilitySerializer<T : Any>(val cap: Capability<T>) : ICapabilitySerializable<INBT> {
    var instance: T = cap.defaultInstance
        ?: throw NullPointerException("Default instance of capability ${cap.name} is null! May be you forgot to add default constructor?")

    @Suppress("unchecked_cast")
    override fun <T : Any> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == this.cap) {
            return LazyOptional.of { instance as T }
        }
        return LazyOptional.empty()
    }

    override fun serializeNBT(): INBT {
        return NBTFormat.serializeNoInline(instance, TypeToken.of(instance.javaClass).rawType)
    }


    override fun deserializeNBT(nbt: INBT) {
        instance = NBTFormat.deserializeNoInline(nbt, TypeToken.of(instance.javaClass).rawType) as T
    }
}

fun <T : HollowCapability> register(clazz: Class<T>) {
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

@Serializable
abstract class HollowCapability(val consumeDataFromClient: Boolean = false) {
}

fun <T : HollowCapability> T.serialize(): INBT {
    return NBTFormat.serializeNoInline(HollowJavaUtils.castDarkMagic(this), this::class.java)
}

fun deserialize(nbt: INBT): HollowCapability {
    return NBTFormat.deserialize(nbt)
}

fun HollowCapability.syncClient(playerEntity: PlayerEntity) {
    if (playerEntity.level.isClientSide && !this.consumeDataFromClient) return //С клиента нельзя обновлять серверную часть

    this.javaClass.createSyncPacketPlayer().send(this, playerEntity)
}

fun HollowCapability.syncEntity(entity: Entity) {
    if (entity.level.isClientSide && !this.consumeDataFromClient) return

    val packet = this.javaClass.createSyncPacketEntity()
    if(entity.level.isClientSide) {
        packet.send(CapabilityForEntity(this, entity.id))
    } else {
        packet.send(CapabilityForEntity(this, entity.id), PacketDistributor.TRACKING_ENTITY.with { entity })
    }
}

fun HollowCapability.syncWorld(vararg players: PlayerEntity) {
    if (players.first().level.isClientSide && !this.consumeDataFromClient) return //С клиента нельзя обновлять серверную часть

    val packet = this.javaClass.createSyncPacketClientLevel()
    players.forEach { packet.send(this, PacketDistributor.PLAYER.with { it as ServerPlayerEntity }) }
}

//Пакет синхронизирующий данные моба у конкретного игрока
fun HollowCapability.syncEntityForPlayer(entity: Entity, player: ServerPlayerEntity) {
    val packet = this.javaClass.createSyncPacketEntity()
    packet.send(CapabilityForEntity(this, entity.id), player)
}

fun HollowCapability.syncServer() {
    this.javaClass.createSyncPacketPlayer().send(this)
}

fun Class<HollowCapability>.createSyncPacketPlayer(): Packet<HollowCapability> = SyncCapabilityPlayer()
fun Class<HollowCapability>.createSyncPacketEntity(): Packet<CapabilityForEntity> = SyncCapabilityEntity()
fun Class<HollowCapability>.createSyncPacketClientLevel(): Packet<HollowCapability> = SyncCapabilityWorld()

fun initCapability(cap: Capability<*>, targets: ArrayList<Type>) {
    HollowCapabilityStorageV2.storages[cap.name] = cap

    targets.forEach {
        val clazz = Class.forName(it.className)
        HollowCapabilityStorageV2.providers.add(clazz to { HollowCapabilitySerializer(cap) })

    }
}
