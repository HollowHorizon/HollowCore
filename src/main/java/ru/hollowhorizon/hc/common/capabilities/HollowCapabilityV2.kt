package ru.hollowhorizon.hc.common.capabilities

import com.google.common.reflect.TypeToken
import kotlinx.serialization.Serializable
import net.minecraft.core.Direction
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityManager
import net.minecraftforge.common.capabilities.CapabilityProvider
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import net.minecraftforge.network.PacketDistributor
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.client.utils.JavaHacks.forceCast
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
                is Player -> it.syncClient(this)
                is Entity -> it.syncEntity(this)
                is Level -> it.syncWorld(*this.players().toTypedArray())
            }
        }
    }
}

@Suppress("UnstableApiUsage")
class HollowCapabilitySerializer<T : Any>(val cap: Capability<T>) : ICapabilitySerializable<Tag> {
    lateinit var instance: T

    @Suppress("unchecked_cast")
    override fun <T : Any> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == this.cap) {
            return LazyOptional.of { instance as T }
        }
        return LazyOptional.empty()
    }

    override fun serializeNBT(): Tag {
        return NBTFormat.serializeNoInline(instance, TypeToken.of(instance.javaClass).rawType)
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserializeNBT(nbt: Tag) {
        instance = NBTFormat.deserializeNoInline(nbt, TypeToken.of(instance.javaClass).rawType) as T
    }
}

fun <T : HollowCapability> register(clazz: Class<T>) {
    FMLJavaModLoadingContext.get().modEventBus.addListener<RegisterCapabilitiesEvent> {
        it.register(clazz)
    }
}

@Serializable
abstract class HollowCapability(val consumeDataFromClient: Boolean = false)

fun <T : HollowCapability> T.serialize(): Tag {
    return NBTFormat.serializeNoInline(forceCast(this), this::class.java)
}

fun deserialize(nbt: Tag): HollowCapability {
    return NBTFormat.deserialize(nbt)
}

fun HollowCapability.syncClient(playerEntity: Player) {
    if (playerEntity.level.isClientSide && !this.consumeDataFromClient) return //С клиента нельзя обновлять серверную часть

    this.javaClass.createSyncPacketPlayer().send(this, playerEntity)
}

fun HollowCapability.syncEntity(entity: Entity) {
    if (entity.level.isClientSide && !this.consumeDataFromClient) return //С клиента нельзя обновлять серверную часть

    val packet = this.javaClass.createSyncPacketEntity()
    if (entity.level.isClientSide) {
        packet.send(CapabilityForEntity(this, entity.id))
    } else {
        packet.send(CapabilityForEntity(this, entity.id), PacketDistributor.TRACKING_ENTITY.with { entity })
    }
}

fun HollowCapability.syncWorld(vararg players: Player) {
    if (players.first().level.isClientSide && !this.consumeDataFromClient) return //С клиента нельзя обновлять серверную часть

    val packet = this.javaClass.createSyncPacketClientLevel()
    players.forEach { packet.send(this, PacketDistributor.PLAYER.with { it as ServerPlayer }) }
}

//Пакет синхронизирующий данные моба у конкретного игрока
fun HollowCapability.syncEntityForPlayer(entity: Entity, player: ServerPlayer) {
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
