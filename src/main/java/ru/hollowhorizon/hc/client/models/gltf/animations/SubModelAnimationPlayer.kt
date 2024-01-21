package ru.hollowhorizon.hc.client.models.gltf.animations

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
import ru.hollowhorizon.hc.client.models.gltf.manager.SubModel
import kotlin.math.pow
import kotlin.math.sqrt


object SubModelPlayer {
    fun update(model: GltfModel, capability: SubModel, currentTick: Int, partialTick: Float) {
        val layers = capability.layers

        model.animationPlayer.nodeModels.parallelStream().forEach { node ->
            node.clearTransform()
            val transform = node.transform.copy()
            layers.forEach {
                val animPose = it.computeTransform(node, model.animationPlayer.nameToAnimationMap, currentTick, partialTick)

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

        capability.layers.removeIf { it.isEnd(currentTick, partialTick) }
    }
}
