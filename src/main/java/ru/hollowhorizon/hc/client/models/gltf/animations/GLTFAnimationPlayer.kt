package ru.hollowhorizon.hc.client.models.gltf.animations

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.animal.Pig
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.vehicle.Boat
import net.minecraft.world.entity.vehicle.Minecart
import net.minecraft.world.phys.Vec3
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.models.gltf.GltfModel
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.LayerMode
import kotlin.math.pow
import kotlin.math.sqrt


open class GLTFAnimationPlayer(val model: GltfModel) {
    private val templates: HashMap<AnimationType, String> = AnimationType.load(model.modelTree)
    val nodeModels = model.modelTree.walkNodes()
    private var currentSpeed = 1f
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
        val switchRot = capability.switchHeadRot
        val modifier = if(entity is Player) 0.1f else 0.2f
        currentSpeed = entity.attributes.getValue(Attributes.MOVEMENT_SPEED).toFloat() / modifier
        if(entity.isShiftKeyDown) currentSpeed *= 0.6f
        head.forEach {
            val newRot = capability.headLayer.computeRotation(entity, switchRot, partialTick)
            it.transform.setRotation(newRot)
        }
    }

    /**
     * Метод, обновляющий все анимации с учётом приоритетов
     */
    fun update(capability: AnimatedEntityCapability, partialTick: Float) {
        val definedLayer = capability.definedLayer
        definedLayer.update(currentLoopAnimation, currentSpeed, currentTick, partialTick)

        //TODO: Вот так складывать Map'ы каждый кадр не хорошо, возможно стоит сделать какое-нибудь кеширование?
        val animationOverrides = typeToAnimationMap + capability.animations.mapNotNull {
            it.key to (nameToAnimationMap[it.value] ?: return@mapNotNull null)
        }.toMap()

        val layers = capability.layers
        nodeModels.forEach { node ->
            node.clearTransform()
            val transform = node.transform.copy()
            definedLayer.computeTransform(node, animationOverrides, currentSpeed, currentTick, partialTick)
                ?.let { animPose ->
                    transform.set(node.fromLocal(animPose))
                }
            layers.forEach {
                val animPose = it.computeTransform(node, nameToAnimationMap, currentTick, partialTick)

                if (animPose != null) {
                    when (it.layerMode) {
                        LayerMode.ADD -> transform.add(animPose)
                        LayerMode.OVERWRITE -> {
                            node.clearTransform()
                            transform.set(node.fromLocal(animPose))
                        }
                    }
                }
            }
            node.transform.set(transform)
        }

        layers.removeIf { it.isEnd(currentTick, partialTick) }
    }

    fun setTick(tick: Int) {
        this.currentTick = tick
    }
}
