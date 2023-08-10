package ru.hollowhorizon.hc.common.capabilities

import com.ibm.icu.lang.UCharacter.GraphemeClusterBreak.T
import kotlinx.serialization.Serializable
import net.minecraft.core.Direction
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.CapabilityProvider
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.fml.Logging
import net.minecraftforge.fml.ModLoader
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.network.PacketDistributor
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.HollowCore
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

        fun <T> get(clazz: Class<T>): Capability<T> {
            return HollowCapabilityStorageV2.storages[clazz.name] as Capability<T>
        }

        @JvmField
        val TYPE = Type.getType(HollowCapabilityV2::class.java)

    }
}

@Suppress("UNCHECKED_CAST")
fun <T> callHook(list: MutableList<ModFileScanData>, method: (String, Boolean) -> Capability<T>) {
    val data = list.flatMap { it.annotations }
    val annotations = data
        .filter { HollowCapabilityV2.TYPE.equals(it.annotationType) }
        .distinct()
        .sortedBy { it.clazz.toString() }

    for (annotation in annotations) {

        HollowCore.LOGGER.debug(Logging.CAPABILITIES, "Attempting to automatically register: {}", annotation)
        val result = method(annotation.clazz.internalName, true)

        val targets: List<Type> =
            (annotation.annotationData["value"] as ArrayList<Type>)
        initCapability(result, targets)
    }

    ModLoader.get().postEvent(RegisterCapabilitiesEvent())
}

fun <T: Any> CapabilityProvider<*>.getCapability(c: KClass<T>): T {
    return this.getCapability(HollowCapabilityV2.get(c.java))
        .orElseThrow { IllegalStateException("Capability ${T::class.java.simpleName} not found!") }
}

@Suppress("unchecked_cast")
class HollowCapabilitySerializer<T : Any>(val cap: Capability<T>) : ICapabilitySerializable<Tag> {
    val type: Class<T> = Class.forName(cap.name.replace("/", ".").replace("$", ".")) as Class<T>
    var instance: T = type.getDeclaredConstructor().newInstance() as T

    override fun <T : Any> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        if (cap == this.cap) {
            return LazyOptional.of { instance as T }
        }
        return LazyOptional.empty()
    }

    override fun serializeNBT(): Tag {
        return NBTFormat.serializeNoInline(instance, type)
    }

    override fun deserializeNBT(nbt: Tag) {
        instance = NBTFormat.deserializeNoInline(nbt, type)
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

fun initCapability(cap: Capability<*>, targets: List<Type>) {
    HollowCore.LOGGER.info("{} for [{}]", cap.name, targets.joinToString { it.internalName })
    HollowCapabilityStorageV2.storages[cap.name.replace("/", ".").replace("$", ".")] = cap

    targets.forEach {
        val clazz = Class.forName(it.className)
        HollowCapabilityStorageV2.providers.add(clazz to { HollowCapabilitySerializer(cap) })

    }
}
