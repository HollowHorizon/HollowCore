package ru.hollowhorizon.hc.client.models.gltf.animations

import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import net.minecraft.util.Mth
import ru.hollowhorizon.hc.client.models.gltf.GltfInterpolation
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import java.util.*
import kotlin.math.acos


object AnimationLoader {

    fun createAnimation(model: GltfTree.GLTFTree, name: String): Animation? {
        return createAnimation(model, model.animations.find { it.name == name } ?: return null)
    }

    @JvmStatic
    fun createAnimation(model: GltfTree.GLTFTree, animationModel: GltfTree.Animation): Animation {
        val animData = animationModel.channels.map { channel ->
            val timeKeys = channel.times.toFloatArray()

            val target = AnimationTarget.valueOf(channel.path.toString().uppercase())
            return@map Pair(target, readAnimationData(
                channel.interpolation,
                target,
                channel.values,
                timeKeys
            ).apply {
                this.node = model.findNodeByIndex(channel.node)
                    ?: throw AnimationException("Node with index ${channel.node} not found!")
            })
        }

        val data = animData.groupBy { it.second.node }
        val result = HashMap<GltfTree.Node, AnimationData>()

        data.forEach { (key, values) ->
            result[key] = AnimationData(
                key,
                values.find { it.first == AnimationTarget.TRANSLATION }?.second,
                values.find { it.first == AnimationTarget.ROTATION }?.second,
                values.find { it.first == AnimationTarget.SCALE }?.second
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
            GltfInterpolation.CUBICSPLINE -> loadCubicSpline(outputData, timeKeys, target)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadCubicSpline(
        outputData: List<Any>,
        keys: FloatArray,
        target: AnimationTarget,
    ): Interpolator<*> {
        return when (target) {
            AnimationTarget.TRANSLATION, AnimationTarget.SCALE -> {
                val size = keys.size
                val array = Array(size) { Array(size) { FloatArray(3) } }
                val data = outputData as List<Vector3f>

                array.forEachIndexed { index, floatArrays ->
                    floatArrays.forEachIndexed { i, floats ->
                        floats[0] = data[index + i].x()
                        floats[1] = data[index + i].y()
                        floats[2] = data[index + i].z()
                    }
                }

                Interpolator.CubicSpline(
                    keys, array
                )
            }

            AnimationTarget.ROTATION -> {
                val size = keys.size
                val array = Array(size) { Array(size) { FloatArray(4) } }
                val data = outputData as List<Vector4f>

                array.forEachIndexed { index, floatArrays ->
                    floatArrays.forEachIndexed { i, floats ->
                        floats[0] = data[index + i].x()
                        floats[1] = data[index + i].y()
                        floats[2] = data[index + i].z()
                        floats[3] = data[index + i].w()
                    }
                }

                Interpolator.CubicSpline(
                    keys, array
                )
            }
        }
    }

    private fun loadStep(outputData: List<Any>, keys: FloatArray, target: AnimationTarget): Interpolator<*> {
        return when (target) {
            AnimationTarget.TRANSLATION -> Interpolator.Step(
                keys,
                outputData.map { (it as Vector3f).array() }.toTypedArray()
            )

            AnimationTarget.ROTATION -> Interpolator.Step(
                keys,
                outputData.map { (it as Vector4f).array() }.toTypedArray()
            )

            AnimationTarget.SCALE -> Interpolator.Step(keys, outputData.map { (it as Vector3f).array() }.toTypedArray())
        }
    }

    private fun loadLinear(
        outputData: List<Any>,
        keys: FloatArray,
        target: AnimationTarget,
    ): Interpolator<*> {
        return when (target) {
            AnimationTarget.TRANSLATION -> Interpolator.Linear(
                keys,
                outputData.map { (it as Vector3f).array() }.toTypedArray()
            )

            AnimationTarget.ROTATION -> Interpolator.SphericalLinear(
                keys,
                outputData.map { (it as Vector4f).array() }.toTypedArray()
            )

            AnimationTarget.SCALE -> Interpolator.Linear(
                keys,
                outputData.map { (it as Vector3f).array() }.toTypedArray()
            )
        }
    }
}

abstract class Interpolator<T>(val keys: FloatArray, val values: Array<T>) {
    abstract fun compute(time: Float): FloatArray

    val maxTime = keys.last()

    val Float.animIndex: Int
        get() {
            val index = Arrays.binarySearch(keys, this)

            return if (index >= 0) index
            else 0.coerceAtLeast(-index - 2)
        }

    lateinit var node: GltfTree.Node

    class Step(keys: FloatArray, values: Array<FloatArray>) : Interpolator<FloatArray>(keys, values) {
        override fun compute(time: Float) = values[time.animIndex].copyOf()

    }

    class Linear(keys: FloatArray, values: Array<FloatArray>) : Interpolator<FloatArray>(keys, values) {
        override fun compute(time: Float): FloatArray {
            if (time <= keys.first() || keys.size == 1) return values.first()
            else if (time >= keys.last()) return values.last()
            else {
                val previousIndex = time.animIndex
                val nextIndex = previousIndex + 1
                val local = time - keys[previousIndex]
                val delta = keys[nextIndex] - keys[previousIndex]
                val alpha = local / delta
                val previousPoint = values[previousIndex]
                val nextPoint = values[nextIndex]
                return FloatArray(previousPoint.size) { i ->
                    val p = previousPoint[i]
                    val n = nextPoint[i]
                    return@FloatArray p + alpha * (n - p)
                }
            }
        }
    }

    class SphericalLinear(keys: FloatArray, values: Array<FloatArray>) : Interpolator<FloatArray>(keys, values) {
        override fun compute(time: Float): FloatArray {
            if (time <= keys.first() || keys.size == 1) return values.first()
            else if (time >= keys.last()) return values.last()
            else {
                val previousIndex = time.animIndex
                val nextIndex = previousIndex + 1

                val local = time - keys[previousIndex]
                val delta = keys[nextIndex] - keys[previousIndex]
                val alpha = local / delta

                val previousPoint = values[previousIndex]
                val nextPoint = values[nextIndex]

                val ax = previousPoint[0]
                val ay = previousPoint[1]
                val az = previousPoint[2]
                val aw = previousPoint[3]
                var bx = nextPoint[0]
                var by = nextPoint[1]
                var bz = nextPoint[2]
                var bw = nextPoint[3]

                var dot = ax * bx + ay * by + az * bz + aw * bw
                if (dot < 0) {
                    bx = -bx
                    by = -by
                    bz = -bz
                    bw = -bw
                    dot = -dot
                }
                val epsilon = 1e-6f
                val s0: Float
                val s1: Float
                if (1.0 - dot > epsilon) {
                    val omega = acos(dot)
                    val invSinOmega = 1.0f / Mth.sin(omega)
                    s0 = Mth.sin((1.0f - alpha) * omega) * invSinOmega
                    s1 = Mth.sin(alpha * omega) * invSinOmega
                } else {
                    s0 = 1.0f - alpha
                    s1 = alpha
                }

                return floatArrayOf(
                    s0 * ax + s1 * bx,
                    s0 * ay + s1 * by,
                    s0 * az + s1 * bz,
                    s0 * aw + s1 * bw
                )
            }
        }

    }

    class CubicSpline(keys: FloatArray, values: Array<Array<FloatArray>>) :
        Interpolator<Array<FloatArray>>(keys, values) {
        override fun compute(time: Float): FloatArray {
            if (time <= keys.first() || keys.size == 1) return values.first()[1]
            else if (time >= keys.last()) return values.last()[1]
            else {
                val previousIndex = time.animIndex
                val nextIndex = previousIndex + 1

                val local = time - keys[previousIndex]
                val delta = keys[nextIndex] - keys[previousIndex]
                val alpha = local / delta
                val alpha2 = alpha * alpha
                val alpha3 = alpha2 * alpha
                val aa = 2 * alpha3 - 3 * alpha2 + 1
                val ab = alpha3 - 2 * alpha2 + alpha
                val ac = -2 * alpha3 + 3 * alpha2
                val ad = alpha3 - alpha2
                val previous = values[previousIndex]
                val next = values[nextIndex]
                val previousPoint = previous[1]
                val nextPoint = next[1]
                val previousOutputTangent = previous[2]
                val nextInputTangent = next[0]

                return FloatArray(previousPoint.size) { i ->
                    val p = previousPoint[i]
                    val pt = previousOutputTangent[i] * delta
                    val n = nextPoint[i]
                    val nt = nextInputTangent[i] * delta
                    return@FloatArray aa * p + ab * pt + ac * n + ad * nt
                }
            }
        }
    }
}