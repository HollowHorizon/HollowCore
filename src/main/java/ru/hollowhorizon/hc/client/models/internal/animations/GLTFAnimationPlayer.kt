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

import net.irisshaders.iris.api.v0.IrisApi
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.client.gui.DebugOverlay
import ru.hollowhorizon.hc.client.models.internal.AnimatedModel
import ru.hollowhorizon.hc.client.models.internal.Node
import ru.hollowhorizon.hc.client.models.internal.Transformation
import ru.hollowhorizon.hc.client.models.internal.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.internal.manager.LayerMode
import ru.hollowhorizon.hc.client.models.internal.manager.Pose
import ru.hollowhorizon.hc.fabric.internal.IrisHelper


open class GLTFAnimationPlayer(val model: AnimatedModel) {
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
    val head by lazy { nodeModels.filter(Node::isHead) }

    fun updateEntity(entity: LivingEntity, capability: AnimatedEntityCapability, partialTick: Float) {
        if (Minecraft.getInstance().isPaused || IrisHelper.isShadowRendering()) return
        val switchRot = capability.switchHeadRot
        currentSpeed = calculateSpeedViaDeltaMovement(entity)
        DebugOverlay.debugText["Current Speed"] = currentSpeed.toString()

        head.forEach {
            val newRot = capability.headLayer.computeRotation(entity, switchRot, partialTick)
            it.transform.addRotationRight(newRot)
        }
    }

    /**
     * Метод, обновляющий все анимации с учётом приоритетов
     */
    fun update(capability: AnimatedEntityCapability) {
        if (Minecraft.getInstance().isPaused || IrisHelper.isShadowRendering()) return
        val definedLayer = capability.definedLayer
        definedLayer.update(currentLoopAnimation, currentSpeed)
        capability.layers.forEach { it.update() }
        val pose = capability.pose
        var rawPose = capability.rawPose

        if (pose != null) {
            if (pose.map.isEmpty()) rawPose?.shouldRemove = true
            else rawPose = Pose(capability.pose!!.map.mapNotNull {
                (model.modelTree.findNodeByIndex(it.key) ?: return@mapNotNull null) to it.value
            }.toMap().toMutableMap())
            capability.rawPose = rawPose
            capability.pose = null
        }

        rawPose?.update()

        val animationOverrides = typeToAnimationMap + capability.animations.mapNotNull {
            it.key to (nameToAnimationMap[it.value] ?: return@mapNotNull null)
        }.toMap()

        val layers = capability.layers
        nodeModels.forEach { node ->
            node.clearTransform()
            val transform = node.transform.copy()
            definedLayer.computeTransform(node, animationOverrides)
                ?.let { animPose ->
                    transform.set(node.fromLocal(animPose))
                }
            node.transform.set(transform)
            layers.forEach {

                when (it.layerMode) {
                    LayerMode.ADD -> {
                        val animPose = it.computeTransform(node, nameToAnimationMap, null)
                        animPose?.let(transform::add)
                    }

                    LayerMode.OVERWRITE -> {
                        it.computeTransform(node, nameToAnimationMap, node.toLocal(transform))
                            ?.let { animPose ->
                                transform.set(node.fromLocal(animPose))
                            }
                    }
                }

            }
            rawPose?.let { transform.add(it.computeTransform(node) ?: Transformation(), false) }
            node.transform.set(transform)
        }

        if (rawPose?.canRemove == true) capability.rawPose = null

        layers.removeIf { it.isEnd() }
    }

    companion object {
        fun calculateSpeedViaDeltaMovement(entity: LivingEntity): Float {
            // 1) берём горизонтальную часть вектора скорости (блоки/тик)
            val vel = entity.deltaMovement
            val dx = vel.x.toFloat()
            val dz = vel.z.toFloat()

            // 2) вектор «вперед» по ориентации тела
            val yawRad = Math.toRadians(entity.yBodyRot.toDouble()).toFloat()
            val forwardX = -Mth.sin(yawRad)
            val forwardZ = Mth.cos(yawRad)

            // 3) проекция вектора скорости на вектор «вперед» (чтобы знать направленную скорость)
            val dot = dx * forwardX + dz * forwardZ

            // 4) переводим блоки/тик → блоки/сек
            return dot * 20f
        }
    }
}
