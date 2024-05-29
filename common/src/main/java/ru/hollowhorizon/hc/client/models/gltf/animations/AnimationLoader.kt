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

package ru.hollowhorizon.hc.client.models.gltf.animations

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector4f
import ru.hollowhorizon.hc.client.models.gltf.GltfInterpolation
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.animations.interpolations.*
import java.util.*


object AnimationLoader {

    fun createAnimation(model: GltfTree.GLTFTree, name: String): Animation? {
        return createAnimation(model, model.animations.find { it.name == name } ?: return null)
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun createAnimation(model: GltfTree.GLTFTree, animationModel: GltfTree.Animation): Animation {
        val animData = animationModel.channels.map { channel ->
            val timeKeys = channel.times.toFloatArray()

            val target = AnimationTarget.valueOf(channel.path.toString().uppercase())

            var values = channel.values

            if(target == AnimationTarget.WEIGHTS) {
                val componentCount = model.findNodeByIndex(channel.node)?.mesh?.weights?.size ?: 0

                fun splitListByN(list: List<Float>, n: Int): List<FloatArray> {
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

                values = splitListByN(values as List<Float>, componentCount)
            }

            return@map Pair(target, readAnimationData(
                channel.interpolation,
                target,
                values,
                timeKeys
            ).apply {
                this.node = model.findNodeByIndex(channel.node)
                    ?: throw AnimationException("Node with index ${channel.node} not found!")
            })
        }

        val data = animData.groupBy { it.second.node }
        val result = Object2ObjectOpenHashMap<GltfTree.Node, AnimationData>()

        data.forEach { (key, values) ->
            result[key] = AnimationData(
                key,
                values.find { it.first == AnimationTarget.TRANSLATION }?.second as? Interpolator<Vector3f>,
                values.find { it.first == AnimationTarget.ROTATION }?.second as? Interpolator<Quaternionf>,
                values.find { it.first == AnimationTarget.SCALE }?.second as? Interpolator<Vector3f>,
                values.find { it.first == AnimationTarget.WEIGHTS }?.second as? Interpolator<FloatArray>
            )
        }

        return Animation(animationModel.name ?: "Unnamed", result)
    }

    private fun readAnimationData(
        interpolation: GltfInterpolation,
        target: AnimationTarget,
        outputData: List<Any>,
        timeKeys: FloatArray,
    ): Interpolator<*> {
        return when (interpolation) {
            GltfInterpolation.STEP -> loadStep(outputData, timeKeys, target)
            GltfInterpolation.LINEAR -> loadLinear(outputData, timeKeys, target)
            GltfInterpolation.CUBICSPLINE -> throw UnsupportedOperationException("Cubic spline interpolation not supported yet!")
        }
    }

    private fun loadStep(outputData: List<Any>, keys: FloatArray, target: AnimationTarget): Interpolator<*> {
        return when (target) {
            AnimationTarget.TRANSLATION -> Vec3Step(keys, outputData.map { it as Vector3f }.toTypedArray())
            AnimationTarget.ROTATION -> QuatStep(keys, outputData.map { (it as Vector4f).asQuaternion }.toTypedArray())
            AnimationTarget.SCALE -> Vec3Step(keys, outputData.map { it as Vector3f }.toTypedArray())
            AnimationTarget.WEIGHTS -> LinearSingle(keys, outputData.map { it as FloatArray }.toTypedArray())
        }
    }

    private fun loadLinear(
        outputData: List<Any>,
        keys: FloatArray,
        target: AnimationTarget,
    ): Interpolator<*> {
        return when (target) {
            AnimationTarget.TRANSLATION -> Linear(keys, outputData.map { it as Vector3f }.toTypedArray())
            AnimationTarget.ROTATION -> SphericalLinear(keys, outputData.map { (it as Vector4f).asQuaternion }.toTypedArray())
            AnimationTarget.SCALE -> Linear(keys, outputData.map { it as Vector3f }.toTypedArray())
            AnimationTarget.WEIGHTS -> LinearSingle(keys, outputData.map { it as FloatArray }.toTypedArray())
        }
    }
}

val Vector4f.asQuaternion get() = Quaternionf(x(), y(), z(), w())