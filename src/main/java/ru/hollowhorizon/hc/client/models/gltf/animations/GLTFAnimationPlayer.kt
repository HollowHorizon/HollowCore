package ru.hollowhorizon.hc.client.models.gltf.animations

import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.client.models.gltf.GltfModel
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.Transformation
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.LayerMode


open class GLTFAnimationPlayer(val model: GltfModel) {
    private val templates: HashMap<AnimationType, String> = AnimationType.load(model.modelPath)
    private val nodeModels = model.modelPath.walkNodes()
    private val bindPose = model.bindPose
    val nameToAnimationMap: Map<String, Animation> = model.modelPath.animations.associate {
        val name = it.name ?: "Unnamed"
        name to AnimationLoader.createAnimation(
            model.modelPath,
            name
        )!!
    }
    val typeToAnimationMap: Map<AnimationType, Animation> =
        templates.mapNotNull { it.key to (nameToAnimationMap[it.value] ?: return@mapNotNull null) }.toMap()
    var currentLoopAnimation = AnimationType.IDLE
    var currentTick = 0
    val head by lazy { nodeModels.filter(GltfTree.Node::isHead) }

    fun updateEntity(entity: LivingEntity, capability: AnimatedEntityCapability, partialTick: Float) {
        head.forEach {
            val newRot = capability.headLayer.computeRotation(entity, partialTick)
            it.transform.setRotation(floatArrayOf(newRot.i(), newRot.j(), newRot.k(), newRot.r()))
        }
    }

    /**
     * Метод, обновляющий все анимации с учётом приоритетов
     */
    fun update(capability: AnimatedEntityCapability, partialTick: Float) {
        val definedLayer = capability.definedLayer
        definedLayer.update(currentLoopAnimation, currentTick, partialTick)
        val animationOverrides = typeToAnimationMap + capability.animations.mapNotNull {
            it.key to (nameToAnimationMap[it.value] ?: return@mapNotNull null)
        }.toMap()

        nodeModels.forEach { node ->
            bindPose.apply(node, 0f)
            val bindPose = bindPose.compute(node, Transformation(), 0f) ?: return@forEach
            val transform = node.transform.copy()
            definedLayer.computeTransform(node, bindPose, animationOverrides, currentTick, partialTick)?.let {
                transform.add(it)
            }
            capability.layers.forEach {
                val animPose = it.computeTransform(node, bindPose, nameToAnimationMap, currentTick, partialTick)

                if (animPose != null) {
                    when (it.layerMode) {
                        LayerMode.ADD -> transform.add(animPose)
                        LayerMode.OVERWRITE -> transform.set(bindPose.copy().apply { add(animPose) })
                    }
                }
            }
            node.transform.set(transform)
        }

        capability.layers.removeIf { it.isEnd(nameToAnimationMap, currentTick, partialTick) }
    }

    fun setTick(tick: Int) {
        this.currentTick = tick
    }
}

fun List<Pair<Float, FloatArray?>>.sumWithPriority(prioritySum: Float): FloatArray? {
    if (this.isEmpty()) return null
    if (this.size == 1) return this.first().second

    val result = FloatArray(this.firstNotNullOfOrNull { it.second }?.size ?: return null)

    this.forEach { entry ->
        val array = entry.second ?: return@forEach

        for (i in array.indices) result[i] += array[i] * entry.first //value * priority
    }

    return result.apply {
        for (i in this.indices) this[i] /= prioritySum
    }
}
