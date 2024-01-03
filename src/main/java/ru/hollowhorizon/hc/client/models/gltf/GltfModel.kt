package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector4f
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.ItemInHandRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.client.models.gltf.animations.Animation
import ru.hollowhorizon.hc.client.models.gltf.animations.GLTFAnimationPlayer
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability


class ModelData(
    val leftHand: ItemStack?,
    val rightHand: ItemStack?,
    val itemInHandRenderer: ItemInHandRenderer?,
    val entity: LivingEntity?,
)

typealias NodeRenderer = (LivingEntity, PoseStack, GltfTree.Node, Int) -> Unit

class GltfModel(val modelTree: GltfTree.GLTFTree) {
    val bindPose = Animation.createFromPose(modelTree.walkNodes())
    val animationPlayer = GLTFAnimationPlayer(this)
    var visuals: NodeRenderer = { _, _, _, _ -> }

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
        consumer: (ResourceLocation) -> RenderType,
        light: Int,
        overlay: Int,
    ) {
        //Получение текущих VAO и IBO
        val currentVAO = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING)
        val currentElementArrayBuffer = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING)

        GL33.glVertexAttrib4f(1, 1.0F, 1.0F, 1.0F, 1.0F) // Цвет
        GL33.glVertexAttribI2i(3, overlay and '\uffff'.code, overlay shr 16 and '\uffff'.code) // Оверлей при ударе
        GL33.glVertexAttribI2i(4, light and '\uffff'.code, light shr 16 and '\uffff'.code) // Освещение

        drawWithShader(GameRenderer.getRendertypeEntityTranslucentShader()!!) {
            modelTree.scenes.forEach { it.render(stack, visuals, modelData, consumer, light, overlay) }
        }

        GL33.glBindVertexArray(currentVAO)
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)

        NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear()
    }
}

private fun Vector4f.isNotEmpty(): Boolean {
    return x() != 0f || y() != 0f || z() != 0f || w() != 0f
}
