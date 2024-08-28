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

@file:Suppress("UNCHECKED_CAST")

package ru.hollowhorizon.hc.common.capabilities

import kotlinx.serialization.Serializable
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.Tag
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.api.ICapabilityDispatcher
import ru.hollowhorizon.hc.client.utils.nbt.ForTag
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.handlers.HollowEventHandler
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hc.common.network.sendAllInDimension
//? if <=1.19.2
import ru.hollowhorizon.hc.client.utils.math.level

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class CSyncEntityCapabilityPacket(
    private val entityId: Int,
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
) : HollowPacketV3<CSyncEntityCapabilityPacket> {
    override fun handle(player: Player) {
        val entity = player.level().getEntity(entityId)
        if (entity == null) {
            HollowEventHandler.ENTITY_TAGS.computeIfAbsent(entityId) { mutableMapOf() }[capability] = value
            return
        }
        val cap = (entity as ICapabilityDispatcher).capabilities.first { it.javaClass.name == capability }

        if ((value as? CompoundTag)?.isEmpty == false) {
            cap.deserializeNBT(value)
        }
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class SSyncEntityCapabilityPacket(
    private val entityId: Int,
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
) : HollowPacketV3<SSyncEntityCapabilityPacket> {
    override fun handle(player: Player) {
        val entity = player.level().getEntity(entityId)
            ?: throw IllegalStateException("Entity with id $entityId not found: $this".apply(HollowCore.LOGGER::warn))
        val cap = (entity as ICapabilityDispatcher).capabilities.first { it.javaClass.name == capability }

        if (cap.canAcceptFromClient(player)) {
            cap.deserializeNBT(value)
            CSyncEntityCapabilityPacket(
                entityId, capability, value
            ).send(*player.level().server!!.playerList.players.toTypedArray())
        }
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class CSyncLevelCapabilityPacket(
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
) : HollowPacketV3<CSyncLevelCapabilityPacket> {
    override fun handle(player: Player) {
        val level = player.level() as ICapabilityDispatcher
        val cap = level.capabilities.first { it.javaClass.name == capability }

        if ((value as? CompoundTag)?.isEmpty == false) {
            cap.deserializeNBT(value)
        }
    }

}

@HollowPacketV2(HollowPacketV2.Direction.TO_SERVER)
@Serializable
class SSyncLevelCapabilityPacket(
    val level: String,
    val capability: String,
    val value: @Serializable(ForTag::class) Tag,
) : HollowPacketV3<SSyncLevelCapabilityPacket> {
    override fun handle(player: Player) {
        val server = player.server ?: throw IllegalStateException("Server not found".apply(HollowCore.LOGGER::warn))
        val levelKey = server.levelKeys().find { it.location() == level.rl }
            ?: throw IllegalStateException("Unknown level: $level".apply(HollowCore.LOGGER::warn))
        val level = server.getLevel(levelKey)
            ?: throw IllegalStateException("Level not found: $level".apply(HollowCore.LOGGER::warn))
        val cap = (level as ICapabilityDispatcher).capabilities.first { it.javaClass.name == capability }

        if (cap.canAcceptFromClient(player)) {
            cap.deserializeNBT(value)
            CSyncLevelCapabilityPacket(capability, value).sendAllInDimension(level)
        }
    }

}