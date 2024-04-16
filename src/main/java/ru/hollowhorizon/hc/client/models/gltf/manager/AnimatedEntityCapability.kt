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

package ru.hollowhorizon.hc.client.models.gltf.manager

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import kotlinx.serialization.Serializable
import net.minecraft.nbt.Tag
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.Transform
import ru.hollowhorizon.hc.client.models.gltf.Transformation
import ru.hollowhorizon.hc.client.models.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.deserialize
import ru.hollowhorizon.hc.client.utils.nbt.serialize
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2

@HollowCapabilityV2(IAnimated::class, Player::class)
class AnimatedEntityCapability : CapabilityInstance() {
    internal val definedLayer = DefinedLayer()
    internal val headLayer = HeadLayer()
    var rawPose: Pose? = null
    var model by syncable("%NO_MODEL%")
    val layers by syncableList<AnimationLayer>()
    val textures by syncableMap<String, String>()
    val animations by syncableMap<AnimationType, String>()
    var transform by syncable(Transform())
    val subModels by syncableMap<String, SubModel>()
    var switchHeadRot by syncable(false)
    var pose: RawPose? by syncable(null)
}

@Serializable
class RawPose(val map: Map<Int, Transformation> = Int2ObjectOpenHashMap()) {
    fun toNBT() = NBTFormat.serialize(this)

    companion object {
        fun fromNBT(tag: Tag) = NBTFormat.deserialize<RawPose>(tag)
    }
}

class Pose(val map: MutableMap<GltfTree.Node, Transformation>) {
    var fadeIn = 0f
    var fadeOut = 0f
    var shouldRemove = false
    private var startTime = 0
    private var endTime = 0
    val canRemove get() = shouldRemove && fadeOut >= 10f

    fun computeTransform(
        node: GltfTree.Node,
    ): Transformation? {
        return if (shouldRemove) {
            Transformation.lerp(
                map[node]?.copy(),
                null,
                fadeOut / 10f
            )
        } else {
            Transformation.lerp(
                null,
                map[node]?.copy(),
                fadeIn / 10f
            )
        }
    }

    fun update(currentTick: Int, partialTick: Float) {
        if (fadeIn < 10f) {
            if(startTime == 0) startTime = currentTick
            fadeIn = currentTick - startTime + partialTick
        } else if (shouldRemove) {
            if(endTime == 0) endTime = currentTick
            fadeOut = currentTick - endTime + partialTick
        }
    }
}