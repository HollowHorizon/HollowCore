package ru.hollowhorizon.hc.client.models.gltf.animations

import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.models.gltf.GltfModel
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.Transformation
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimationLayer


open class GLTFAnimationPlayer(val model: GltfModel) {
    val templates: HashMap<AnimationType, String> = AnimationType.load(model.modelPath)
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

        nodeModels.forEach { node ->
            bindPose.apply(node, 0f)
            val transforms = HashMap<Transformation, Float>()
            definedLayer.computeTransform(node, bindPose, typeToAnimationMap, currentTick, partialTick)?.let {
                transforms.put(it, 1.0f)
            }
            transforms += capability.layers.mapNotNull {
                (it.computeTransform(node, nameToAnimationMap, currentTick, partialTick)
                    ?: return@mapNotNull null) to it.priority
            }.toMap()
            transforms += capability.onceAnimations.mapNotNull {
                (it.computeTransform(node, nameToAnimationMap, currentTick, partialTick)
                    ?: return@mapNotNull null) to it.priority + 10f
            }.toMap()
            node.transform.set(transforms)
        }

        capability.onceAnimations.removeIf { it.isEnd(nameToAnimationMap, currentTick, partialTick) }
    }

    fun setTick(tick: Int) {
        this.currentTick = tick
    }

    fun playOnce(capability: AnimatedEntityCapability, type: AnimationType) {
        templates[type]?.let { playOnce(capability, it) }
    }

    fun playOnce(capability: AnimatedEntityCapability, name: String) {
        if (capability.onceAnimations.any { it.animation == name }) return
        capability.onceAnimations.add(AnimationLayer(name, 1.0f, PlayType.ONCE, 1.0f, 0))
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
