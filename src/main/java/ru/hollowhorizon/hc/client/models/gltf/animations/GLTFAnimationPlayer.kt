package ru.hollowhorizon.hc.client.models.gltf.animations

import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.client.models.gltf.GltfModel
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.LayerMode


open class GLTFAnimationPlayer(val model: GltfModel) {
    private val templates: HashMap<AnimationType, String> = AnimationType.load(model.modelTree)
    private val nodeModels = model.modelTree.walkNodes()
    val nameToAnimationMap: Map<String, Animation> = model.modelTree.animations.associate {
        val name = it.name ?: "Unnamed"
        name to AnimationLoader.createAnimation(
            model.modelTree,
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
            it.transform.setRotation(newRot)
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
            node.clearTransform()
            val transform = node.transform.copy()
            definedLayer.computeTransform(node, animationOverrides, currentTick, partialTick)?.let { animPose ->
                transform.set(node.fromLocal(animPose))
            }
            capability.layers.forEach {
                val animPose = it.computeTransform(node, nameToAnimationMap, currentTick, partialTick)

                if (animPose != null) {
                    when (it.layerMode) {
                        LayerMode.ADD -> transform.add(animPose)
                        LayerMode.OVERWRITE -> transform.set(node.fromLocal(animPose))
                    }
                }
            }
            node.transform.set(transform)
        }

        capability.layers.removeIf { it.isEnd(currentTick, partialTick) }
    }

    fun setTick(tick: Int) {
        this.currentTick = tick
    }
}
