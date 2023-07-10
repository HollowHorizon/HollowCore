package ru.hollowhorizon.hc.client.gltf.animations

import de.javagl.jgltf.model.AccessorData
import de.javagl.jgltf.model.AccessorFloatData
import de.javagl.jgltf.model.AnimationModel
import de.javagl.jgltf.model.NodeModel
import de.javagl.jgltf.model.io.GltfModelReader
import net.minecraft.util.math.MathHelper
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.capabilities.AnimatedEntityCapability
import java.util.*
import kotlin.math.acos

object AnimationLoader {
    @JvmStatic
    fun main(args: Array<String>) {
        val c = AnimatedEntityCapability()
        val model = GltfModelReader().readWithoutReferences(c, HollowJavaUtils.getResource(c.model.rl))
        model.animationModels.forEach { createAnimation(it) }
    }

    @JvmStatic
    fun createAnimation(animationModel: AnimationModel): Animation {
        val animData = animationModel.channels.mapNotNull { channel ->
            val sampler = channel.sampler
            val timeKeys = sampler.input.accessorData.values

            val outputData = sampler.output.accessorData
            if (outputData !is AccessorFloatData) {
                HollowCore.LOGGER.warn(
                    "Output data is not an AccessorFloatData, but "
                            + outputData.javaClass
                )
                return@mapNotNull null
            }

            val target = AnimationTarget.valueOf(channel.path.uppercase())
            return@mapNotNull AnimationData(target, readAnimationData(sampler, target, outputData, timeKeys).apply {
                this.node = channel.nodeModel
            })
        }

        return Animation(animData.groupBy { it.interpolator.node })
    }

    private fun readAnimationData(
        sampler: AnimationModel.Sampler,
        target: AnimationTarget,
        outputData: AccessorFloatData,
        timeKeys: FloatArray,
    ): Interpolator<*> {
        val componentCount = outputData.numComponentsPerElement

        return when (val interpolation = sampler.interpolation) {
            AnimationModel.Interpolation.STEP -> loadStep(outputData, timeKeys, componentCount)
            AnimationModel.Interpolation.LINEAR -> loadLinear(
                outputData,
                timeKeys,
                componentCount,
                target == AnimationTarget.ROTATION
            )

            AnimationModel.Interpolation.CUBICSPLINE -> loadCubicSpline(outputData, timeKeys, componentCount)

            else -> throw AnimationException("Interpolation type not supported: $interpolation")
        }
    }

    private fun loadCubicSpline(
        outputData: AccessorFloatData,
        keys: FloatArray,
        componentCount: Int
    ): Interpolator<*> {
        val elementCount = keys.size
        val valuesCubic = Array(elementCount) { Array(3) { FloatArray(componentCount) } }

        for (e in 0 until elementCount) {
            for (i in 0 until 3) {
                for (c in 0 until componentCount) {
                    valuesCubic[e][i][c] = outputData[e * 3 + i, c]
                }
            }
        }


        return Interpolator.CubicSpline(keys, valuesCubic)
    }

    private fun loadStep(outputData: AccessorFloatData, keys: FloatArray, componentCount: Int): Interpolator<*> {
        val elementCount = keys.size
        val values = Array(elementCount) { FloatArray(componentCount) }

        for (e in 0 until elementCount) {
            for (c in 0 until componentCount) {
                values[e][c] = outputData[e, c]
            }
        }
        return Interpolator.Step(keys, values)
    }

    private fun loadLinear(
        outputData: AccessorFloatData,
        keys: FloatArray,
        componentCount: Int,
        isRotation: Boolean
    ): Interpolator<*> {
        val elementCount = keys.size
        val values = Array(elementCount) { FloatArray(componentCount) }

        for (e in 0 until elementCount) {
            for (c in 0 until componentCount) {
                values[e][c] = outputData[e, c]
            }
        }
        return if (isRotation) Interpolator.SphericalLinear(keys, values) else Interpolator.Linear(keys, values)
    }
}

abstract class Interpolator<T>(val keys: FloatArray, val values: Array<T>) {
    abstract fun compute(time: Float): FloatArray

    val Float.animIndex: Int
        get() {
            val index = Arrays.binarySearch(keys, this)

            return if (index >= 0) {
                index
            } else 0.coerceAtLeast(-index - 2)
        }

    lateinit var node: NodeModel

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
                    val invSinOmega = 1.0f / MathHelper.sin(omega)
                    s0 = MathHelper.sin((1.0f - alpha) * omega) * invSinOmega
                    s1 = MathHelper.sin(alpha * omega) * invSinOmega
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

val AccessorData.values: FloatArray
    get() {
        if (this !is AccessorFloatData) throw AnimationException("[GLTFLoader] Accessor data is not an AccessorFloatData, but ${this.javaClass}")
        val numKeyElements = this.numElements
        val keys = FloatArray(numKeyElements)

        for (e in 0 until numKeyElements) keys[e] = this[e]
        return keys
    }