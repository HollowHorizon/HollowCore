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

package ru.hollowhorizon.hc.client.models.internal.animations

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import ru.hollowhorizon.hc.client.models.gltf.*
import ru.hollowhorizon.hc.client.models.internal.Model
import ru.hollowhorizon.hc.client.models.internal.Node
import ru.hollowhorizon.hc.client.models.internal.animations.interpolations.*


object AnimationLoader {

    fun createAnimation(model: Model, name: String): Animation? {
        return createAnimation(model, model.animations.find { it.name == name } ?: return null)
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun createAnimation(model: Model, animationModel: ru.hollowhorizon.hc.client.models.internal.Animation): Animation {
        val animData = animationModel.channels.map { channel ->
            val timeKeys = channel.times.toFloatArray()

            val target = AnimationTarget.valueOf(channel.path.uppercase())

            val size = if (target == AnimationTarget.WEIGHTS) {
                model.findNodeByIndex(channel.node)?.mesh?.weights?.size ?: 0
            } else -1

            return@map Pair(target, readAnimationData(
                channel.interpolation,
                target,
                channel.values,
                timeKeys,
                size
            ).apply {
                this.node = model.findNodeByIndex(channel.node)
                    ?: throw AnimationException("Node with index ${channel.node} not found!")
            })
        }

        val data = animData.groupBy { it.second.node }
        val result = Object2ObjectOpenHashMap<Node, AnimationData>()

        data.forEach { (key, values) ->
            result[key] = AnimationData(
                key,
                values.find { it.first == AnimationTarget.TRANSLATION }?.second as? Interpolator<Vector3f>,
                values.find { it.first == AnimationTarget.ROTATION }?.second as? Interpolator<Vector4f>,
                values.find { it.first == AnimationTarget.SCALE }?.second as? Interpolator<Vector3f>,
                values.find { it.first == AnimationTarget.WEIGHTS }?.second as? Interpolator<FloatArray>
            )
        }

        return Animation(animationModel.name ?: "Unnamed", result)
    }

    private fun readAnimationData(
        interpolation: String,
        target: AnimationTarget,
        outputData: GltfAccessor,
        timeKeys: FloatArray,
        componentCount: Int = -1,
    ): Interpolator<*> {
        return when (interpolation) {
            GltfAnimation.Sampler.INTERPOLATION_STEP -> loadStep(outputData, timeKeys, target, componentCount)
            GltfAnimation.Sampler.INTERPOLATION_LINEAR -> loadLinear(outputData, timeKeys, target, componentCount)
            else -> throw UnsupportedOperationException("Animation type $interpolation not supported yet!")
        }
    }

    private fun loadStep(
        outputData: GltfAccessor,
        keys: FloatArray,
        target: AnimationTarget,
        componentCount: Int = -1,
    ): Interpolator<*> {
        return when (target) {
            AnimationTarget.TRANSLATION -> Vec3Step(keys, Vec3fAccessor(outputData).list)
            AnimationTarget.ROTATION -> QuatStep(keys, Vec4fAccessor(outputData).list)
            AnimationTarget.SCALE -> Vec3Step(keys, Vec3fAccessor(outputData).list)
            AnimationTarget.WEIGHTS -> LinearSingle(
                keys,
                splitListByN(FloatAccessor(outputData).list.toList(), componentCount).toTypedArray()
            )
        }
    }

    private fun loadLinear(
        outputData: GltfAccessor,
        keys: FloatArray,
        target: AnimationTarget,
        componentCount: Int = -1,
    ): Interpolator<*> {
        return when (target) {
            AnimationTarget.TRANSLATION -> Linear(keys, Vec3fAccessor(outputData).list)
            AnimationTarget.ROTATION -> SphericalLinear(keys, Vec4fAccessor(outputData).list)
            AnimationTarget.SCALE -> Linear(keys, Vec3fAccessor(outputData).list)
            AnimationTarget.WEIGHTS -> LinearSingle(
                keys,
                splitListByN(FloatAccessor(outputData).list.toList(), componentCount).toTypedArray()
            )
        }
    }
}

fun splitListByN(list: List<Float>, n: Int): List<FloatArray> {
    if (n < 1) return listOf(list.toFloatArray())

    val result = mutableListOf<FloatArray>()
    var startIndex = 0
    while (startIndex < list.size) {
        val endIndex = kotlin.math.min(startIndex + n, list.size)
        val subList = list.subList(startIndex, endIndex).toFloatArray()
        result.add(subList)
        startIndex = endIndex
    }
    return result
}

val Vector4f.asQuaternion get() = Quaternionf(x(), y(), z(), w())