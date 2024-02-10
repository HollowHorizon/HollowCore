package ru.hollowhorizon.hc.common.capabilities

import dev.ftb.mods.ftbteams.FTBTeamsAPI
import dev.ftb.mods.ftbteams.data.Team
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.EndTag
import net.minecraft.nbt.Tag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.chunk.LevelChunk
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.common.capabilities.ICapabilitySerializable
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserializeNoInline
import ru.hollowhorizon.hc.client.utils.nbt.serializeNoInline

@Suppress("API_STATUS_INTERNAL")
open class CapabilityInstance : ICapabilitySerializable<Tag> {
    val properties = Object2ObjectOpenHashMap<String, Any?>()
    open val consumeOnServer: Boolean = false
    open val canOtherPlayersAccess: Boolean = true
    lateinit var provider: ICapabilityProvider //Будет инициализированно инжектом
    lateinit var capability: Capability<CapabilityInstance> //Будет инициализированно инжектом

    fun <T> syncable(default: T) = CapabilityProperty<CapabilityInstance, T>(default)


    fun sync() {
        when (val target = provider) {
            is Entity -> {
                val isPlayer = target is Player
                if (target.level.isClientSide) {
                    if (consumeOnServer) SSyncEntityCapabilityPacket(target.id, capability.name, serializeNBT()).send()
                } else CSyncEntityCapabilityPacket(target.id, capability.name, serializeNBT()).send(
                    when {
                        !isPlayer -> PacketDistributor.TRACKING_ENTITY.with { target }
                        canOtherPlayersAccess -> PacketDistributor.TRACKING_ENTITY_AND_SELF.with { target }
                        else -> PacketDistributor.PLAYER.with { target as ServerPlayer }
                    }
                )

            }

            is Team -> {
                target.save()
                target.onlineMembers.forEach {
                    FTBTeamsAPI.getManager().syncAllToPlayer(it, target)
                }
            }

            is BlockEntity -> {
                target.setChanged()
            }

            is LevelChunk -> {
                target.isUnsaved = true
            }

            is Level -> {
                if (target.isClientSide) {
                    if (consumeOnServer) SSyncLevelCapabilityPacket(
                        target.dimension().location().toString(),
                        capability.name,
                        serializeNBT()
                    ).send()
                } else CSyncLevelCapabilityPacket(
                    capability.name,
                    serializeNBT()
                ).send(PacketDistributor.DIMENSION.with { target.dimension() })
            }
        }
    }

    override fun <T : Any?> getCapability(cap: Capability<T>, side: Direction?): LazyOptional<T> {
        return capability.orEmpty(cap, LazyOptional.of { this })
    }

    override fun serializeNBT(): Tag {
        val nbt = CompoundTag()
        properties.forEach { (name, value) ->
            if (value == null) nbt.put(name, EndTag.INSTANCE)
            else {
                nbt.putString("$name%%class", value.javaClass.name)

                when (value) {
                    is SyncableListImpl<*> -> {
                        nbt.put(name, value.serializeNBT())
                    }

                    is SyncableMapImpl<*, *> -> {
                        nbt.put(name, value.serializeNBT())
                    }

                    else -> nbt.put(name, NBTFormat.serializeNoInline(value, value.javaClass))
                }
            }
        }
        return nbt
    }

    override fun deserializeNBT(nbt: Tag) {
        properties.clear()
        if (nbt is CompoundTag) {
            nbt.allKeys.filter { !it.endsWith("%%class") }.forEach { name ->
                try {
                    val value = nbt.get(name)

                    //Вообще говоря не самое хорошее решение, но в теории подсунуть тут другой класс нельзя
                    //Потому что по умолчанию капабилити при пакете на сервер не будут сериализоваться.
                    //А если кому-то это нужно будет, то выйдет ошибка
                    val type = Class.forName(nbt.getString("$name%%class"))

                    when {
                        type == SyncableListImpl::class.java -> {
                            val list = SyncableListImpl(ArrayList(), this::sync)
                            list.deserializeNBT(value!!)
                            properties[name] = list
                            return@forEach
                        }

                        type == SyncableMapImpl::class.java -> {
                            val map = SyncableMapImpl(HashMap(), this::sync)
                            map.deserializeNBT(value!!)
                            properties[name] = map
                            return@forEach
                        }

                        value == null || value is EndTag -> properties[name] = null
                        else -> properties[name] = NBTFormat.deserializeNoInline(value, type)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun <T : Any> syncableList(list: MutableList<T> = ArrayList()) = syncable(SyncableListImpl(list, this::sync))

    fun <T : Any> syncableList(vararg elements: T) = syncableList(elements.toMutableList())

    fun <K : Any, V : Any> syncableMap() = syncable(SyncableMapImpl<K, V>(HashMap(), this::sync))
}