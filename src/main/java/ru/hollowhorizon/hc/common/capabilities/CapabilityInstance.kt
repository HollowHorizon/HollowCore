/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.common.capabilities

import dev.ftb.mods.ftbteams.data.Team
import net.minecraft.core.Direction
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
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
import net.minecraftforge.fml.ModList
import net.minecraftforge.network.PacketDistributor

@Suppress("API_STATUS_INTERNAL")
open class CapabilityInstance : ICapabilitySerializable<Tag> {
    val properties = ArrayList<CapabilityProperty<CapabilityInstance, *>>()
    var notUsedTags = CompoundTag()
    open val consumeOnServer: Boolean = false
    open val canOtherPlayersAccess: Boolean = true
    lateinit var provider: ICapabilityProvider //Будет инициализированно инжектом
    lateinit var capability: Capability<CapabilityInstance> //Будет инициализированно инжектом

    fun <T> syncable(default: T) = CapabilityProperty<CapabilityInstance, T>(default).apply {
        properties += this
    }


    fun sync() {
        if (ModList.get().isLoaded("ftbteams")) {
            val target = provider
            if (target is Team) {
                target.save()
                target.onlineMembers.forEach {
                    CSyncTeamCapabilityPacket(
                        capability.name,
                        serializeNBT()
                    ).send(PacketDistributor.PLAYER.with { it })
                }
            }
        }

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

    override fun serializeNBT() = notUsedTags.copy().apply {
        properties.forEach { it.serialize(this) }
    }

    override fun deserializeNBT(nbt: Tag) {
        properties.forEach { if (it.deserialize(nbt as? CompoundTag ?: return)) nbt.remove(it.defaultName) }
        val tag = nbt as? CompoundTag ?: return
        notUsedTags.mergeData(tag)
    }

    fun CompoundTag.mergeData(other: CompoundTag) {
        other.allKeys.forEach { key ->
            when (val value = this[key]) {
                is ListTag -> value.addAll(other[key] as ListTag)
                is CompoundTag -> value.mergeData(other[key] as CompoundTag)
                else -> this.put(key, other[key] ?: return)
            }
        }
    }

    inline fun <reified T : Any> syncableList(list: MutableList<T> = ArrayList()) =
        syncable(SyncableListImpl(list, T::class.java, this::sync))

    inline fun <reified T : Any> syncableList(vararg elements: T) = syncableList(elements.toMutableList())

    inline fun <reified K : Any, reified V : Any> syncableMap() =
        syncable(SyncableMapImpl(HashMap(), K::class.java, V::class.java, this::sync))
}