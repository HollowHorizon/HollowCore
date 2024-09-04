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

package ru.hollowhorizon.hc.common.network.packets

import kotlinx.serialization.Serializable
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.client.models.internal.animations.AnimationState
import ru.hollowhorizon.hc.client.models.internal.animations.PlayMode
import ru.hollowhorizon.hc.client.models.internal.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.internal.manager.AnimationLayer
import ru.hollowhorizon.hc.client.models.internal.manager.IAnimated
import ru.hollowhorizon.hc.client.models.internal.manager.LayerMode
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
//? if <=1.19.2
import ru.hollowhorizon.hc.client.utils.math.level


@HollowPacketV2
@Serializable
class StartAnimationPacket(
    private val entityId: Int,
    private val name: String,
    private val layerMode: LayerMode,
    private val playType: PlayMode,
    private val speed: Float = 1.0f,
) : HollowPacketV3<StartAnimationPacket> {
    override fun handle(player: Player) {
        player.level().getEntity(entityId)?.let { entity ->
            if (entity is IAnimated || entity is Player) {
                val capability = entity[AnimatedEntityCapability::class]
                if (capability.layers.any { it.animation == name }) return@let

                capability.layers += AnimationLayer(
                    name,
                    layerMode,
                    playType,
                    speed,
                    0
                )
            }
        }
    }

}

@HollowPacketV2
@Serializable
class StopAnimationPacket(
    private val entityId: Int,
    val name: String,
) : HollowPacketV3<StopAnimationPacket> {
    override fun handle(player: Player) {
        player.level().getEntity(entityId)?.let { entity ->
            if (entity is IAnimated || entity is Player) {
                val capability = entity[AnimatedEntityCapability::class]

                capability.layers.filter { it.animation == name }.forEach {
                    it.state = AnimationState.FINISHED
                }
            }
        }
    }

}