package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Matrix4f
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.ItemInHandRenderer
import net.minecraft.client.renderer.block.model.ItemTransforms
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.models.gltf.animations.Animation
import ru.hollowhorizon.hc.client.models.gltf.animations.GLTFAnimationPlayer
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability


class ModelData(
    val leftHand: ItemStack?,
    val rightHand: ItemStack?,
    val itemInHandRenderer: ItemInHandRenderer?,
    val entity: LivingEntity?,
)

class GltfModel(val modelPath: GltfTree.GLTFTree) {
    val bindPose = Animation.createFromPose(modelPath.walkNodes())
    val animationPlayer = GLTFAnimationPlayer(this)
    private val renderCommands = modelPath.scenes.flatMap { scene ->
        val commands =
            ArrayList<(PoseStack, ModelData, (ResourceLocation) -> VertexConsumer, Int, Int) -> Unit>()
        scene.nodes.forEach { createNodeCommands(it, commands) }
        return@flatMap commands
    }.toSet()
    var visuals: ((LivingEntity, PoseStack, GltfTree.Node, Int) -> Unit)? = null

    fun createNodeCommands(
        node: GltfTree.Node,
        commands: MutableList<(PoseStack, ModelData, (ResourceLocation) -> VertexConsumer, Int, Int) -> Unit>,
    ) {
        val computeSkinMatrixCommands = ArrayList<(Matrix4f) -> Matrix4f>()

        commands += cmd@{ stack: PoseStack, modelData: ModelData, consumer: (ResourceLocation) -> VertexConsumer, light: Int, overlay: Int ->
            val entity = modelData.entity ?: return@cmd

            visuals?.invoke(entity, stack, node, light)
        }

        if (node.skin != null) {
            val skin = node.skin
            val joints = skin.joints
            val matrices = skin.inverseBindMatrices

            joints.values.forEachIndexed { i, joint ->
                computeSkinMatrixCommands += { matrix ->
                    val inverseTransform = matrix.copy().apply { invert() }
                    val jointMat = joint.transformationMatrix.apply { multiply(matrices[i]) }
                    inverseTransform.apply { multiply(jointMat) }
                }
            }
        }
        if (node.mesh != null) {
            val mesh = node.mesh
            mesh.primitives.forEach { primitive ->
                primitive.indices?.let { indices ->
                    indices.get<Int>().forEach { index ->
                        val position = primitive.attributes[GltfAttribute.POSITION]?.get<Vector3f>(index) ?: return
                        val normal = primitive.attributes[GltfAttribute.NORMAL]?.get<Vector3f>(index) ?: Vector3f()
                        val texCords =
                            primitive.attributes[GltfAttribute.TEXCOORD_0]?.get<Pair<Float, Float>>(index) ?: Pair(
                                0f,
                                0f
                            )
                        val joints = primitive.attributes[GltfAttribute.JOINTS_0]?.get<Vector4f>(index) ?: Vector4f()
                        val weights = primitive.attributes[GltfAttribute.WEIGHTS_0]?.get<Vector4f>(index) ?: Vector4f()

                        commands += { stack: PoseStack, modelData: ModelData, consumer: (ResourceLocation) -> VertexConsumer, light: Int, overlay: Int ->
                            val buffer = consumer(primitive.material)
                            val transformed = Vector4f()
                            val mat = node.transformationMatrix

                            if (weights.isNotEmpty()) {
                                val first = Vector4f(position)
                                first.transform(computeSkinMatrixCommands[joints.x().toInt()](mat))
                                transformed.add(
                                    first.x() * weights.x(),
                                    first.y() * weights.x(),
                                    first.z() * weights.x(),
                                    0.0f
                                )
                                val second = Vector4f(position)
                                second.transform(computeSkinMatrixCommands[joints.y().toInt()](mat))
                                transformed.add(
                                    second.x() * weights.y(),
                                    second.y() * weights.y(),
                                    second.z() * weights.y(),
                                    0.0f
                                )
                                val third = Vector4f(position)
                                third.transform(computeSkinMatrixCommands[joints.z().toInt()](mat))
                                transformed.add(
                                    third.x() * weights.z(),
                                    third.y() * weights.z(),
                                    third.z() * weights.z(),
                                    0.0f
                                )
                                val fourth = Vector4f(position)
                                fourth.transform(computeSkinMatrixCommands[joints.w().toInt()](mat))
                                transformed.add(
                                    fourth.x() * weights.w(),
                                    fourth.y() * weights.w(),
                                    fourth.z() * weights.w(),
                                    0.0f
                                )
                            } else transformed.apply { set(position.x(), position.y(), position.z(), 1.0f) }
                                .transform(mat)

                            buffer.apply {
                                vertex(stack.last().pose(), transformed.x(), transformed.y(), transformed.z())
                                color(1.0f, 1.0f, 1.0f, 1.0f)
                                uv(texCords.first, texCords.second)
                                overlayCoords(overlay)
                                uv2(light)
                                normal(stack.last().normal(), normal.x(), normal.y(), normal.z())
                                endVertex()
                            }
                        }
                    }
                }
            }
        }
        node.children.forEach { createNodeCommands(it, commands) }
    }

    fun update(capability: AnimatedEntityCapability, currentTick: Int, partialTick: Float) {
        animationPlayer.setTick(currentTick)
        animationPlayer.update(capability, partialTick)
    }

    fun entityUpdate(entity: LivingEntity, capability: AnimatedEntityCapability, partialTick: Float) {
        animationPlayer.updateEntity(entity, capability, partialTick)
    }

    fun render(
        stack: PoseStack,
        modelData: ModelData,
        consumer: (ResourceLocation) -> VertexConsumer,
        light: Int,
        overlay: Int,
    ) {
        renderCommands.forEach { it(stack, modelData, consumer, light, overlay) }
    }
}

private fun Vector4f.isNotEmpty(): Boolean {
    return x() != 0f || y() != 0f || z() != 0f || w() != 0f
}
