package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import ru.hollowhorizon.hc.client.models.gltf.animations.Animation
import ru.hollowhorizon.hc.client.utils.math.plusAssign


open class GltfModel(val model: GltfTree.GLTFTree) {
    val bindPose = Animation.createFromPose(model.walkNodes())

    fun render(stack: PoseStack, buffer: VertexConsumer, light: Int, overlay: Int) {
        model.scenes.forEach { scene ->
            scene.nodes.forEach { node ->
                renderNode(node, stack, buffer, light, overlay)
            }
        }
    }

    private fun renderNode(node: GltfTree.Node, stack: PoseStack, buffer: VertexConsumer, light: Int, overlay: Int) {
        if (node.mesh != null) renderMesh(node.mesh, node, stack, buffer, light, overlay)
        node.children.forEach { renderNode(it, stack, buffer, light, overlay) }

    }

    private fun renderMesh(
        mesh: GltfTree.Mesh,
        node: GltfTree.Node,
        stack: PoseStack,
        buffer: VertexConsumer,
        light: Int,
        overlay: Int,
    ) {
        mesh.primitives.forEach { primitive ->
            primitive.indices?.let { indices ->
                indices.get<Int>().forEach { index ->
                    val position = primitive.attributes[GltfAttribute.POSITION]?.get<Vector3f>(index) ?: return
                    val normal = primitive.attributes[GltfAttribute.NORMAL]?.get<Vector3f>(index) ?: Vector3f()
                    val texCords =
                        primitive.attributes[GltfAttribute.TEXCOORD_0]?.get<Pair<Float, Float>>(index) ?: Pair(0f, 0f)
                    val joints = primitive.attributes[GltfAttribute.JOINTS_0]?.get<Vector4f>(index) ?: Vector4f()
                    val weights = primitive.attributes[GltfAttribute.WEIGHTS_0]?.get<Vector4f>(index) ?: Vector4f()

                    renderPrimitive(stack, buffer, node, position, normal, texCords, joints, weights, light, overlay)
                }
            }
        }
    }

    private fun renderPrimitive(
        stack: PoseStack,
        buffer: VertexConsumer,
        initialNode: GltfTree.Node,
        positionRaw: Vector3f,
        normal: Vector3f,
        texCords: Pair<Float, Float>,
        joints: Vector4f,
        weights: Vector4f, light: Int, overlay: Int,
    ) {
        val position = Vector4f(positionRaw)
        position.transform(initialNode.computeMatrix())

        buffer.apply {
            vertex(stack.last().pose(), position.x(), position.y(), position.z())
            color(1.0f, 1.0f, 1.0f, 0.9f)
            uv(texCords.first, texCords.second)
            overlayCoords(overlay)
            uv2(light)
            normal(stack.last().normal(), normal.x(), normal.y(), normal.z())
            endVertex()
        }
    }
}
