package ru.hollowhorizon.hc.client.models.gltf.animations

import com.mojang.math.Quaternion
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
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
        val result = Object2ObjectOpenHashMap<GltfTree.Node, AnimationData>()

        data.forEach { (key, values) ->
            result[key] = AnimationData(
                key,
                values.find { it.first == AnimationTarget.TRANSLATION }?.second as? Interpolator<Vector3f>,
                values.find { it.first == AnimationTarget.ROTATION }?.second as? Interpolator<Quaternion>,
                values.find { it.first == AnimationTarget.SCALE }?.second as? Interpolator<Vector3f>
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
            else -> throw UnsupportedOperationException("")
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
            else -> throw UnsupportedOperationException("")
        }
    }
}

val Vector4f.asQuaternion get() = Quaternion(x(), y(), z(), w())