package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector4f
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.ItemInHandRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.client.models.gltf.animations.GLTFAnimationPlayer
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.client.utils.areShadersEnabled
import ru.hollowhorizon.hc.common.registry.ModShaders


class ModelData(
    val leftHand: ItemStack?,
    val rightHand: ItemStack?,
    val itemInHandRenderer: ItemInHandRenderer?,
    val entity: LivingEntity?,
)

typealias NodeRenderer = (LivingEntity, PoseStack, GltfTree.Node, Int) -> Unit

class GltfModel(val modelTree: GltfTree.GLTFTree) {
    val nodes = modelTree.walkNodes().associateBy { (it.name ?: "Unnamed") }
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
        consumer: (ResourceLocation) -> RenderType.CompositeRenderType,
        light: Int,
        overlay: Int,
    ) {
        modelTree.scenes.forEach {
            it.nodes.forEach { node ->
                node.renderDecorations(stack, visuals, modelData, light)
            }
        }

        val texBind = GL33.glGetInteger(GL33.GL_ACTIVE_TEXTURE)
        CURRENT_NORMAL = stack.last().normal()

        val p = GL11.glGetInteger(GL33.GL_CURRENT_PROGRAM)
        transformSkinning(stack)
        GL33.glUseProgram(p)

        //Получение текущих VAO и IBO
        val currentVAO = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING)
        val currentElementArrayBuffer = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING)

        GL33.glVertexAttrib4f(1, 1.0F, 1.0F, 1.0F, 1.0F) // Цвет
        GL33.glVertexAttribI2i(3, overlay and '\uffff'.code, overlay shr 16 and '\uffff'.code) // Оверлей при ударе
        GL33.glVertexAttribI2i(4, light and '\uffff'.code, light shr 16 and '\uffff'.code) // Освещение

        GL13.glActiveTexture(GL13.GL_TEXTURE2) //Лайтмап
        val currentTexture2 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, GltfManager.lightTexture.id)

        GL13.glActiveTexture(GL13.GL_TEXTURE1) //Оверлей
        val currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
        Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, RenderSystem.getShaderTexture(1))
        Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor()

        GL13.glActiveTexture(GL13.GL_TEXTURE0) //Текстуры модели
        val currentTexture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)

        drawWithShader(if (!areShadersEnabled) ModShaders.GLTF_ENTITY else GameRenderer.getRendertypeEntityTranslucentShader()!!) {
            modelTree.scenes.forEach { it.render(stack, visuals, modelData, consumer, light) }
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE2) //Возврат Лайтмапа
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture2)
        GL13.glActiveTexture(GL13.GL_TEXTURE1) //Возврат Оверлея
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1)
        GL13.glActiveTexture(GL13.GL_TEXTURE0) //Возврат Исходных текстур
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture0)
        GL13.glActiveTexture(texBind)

        GL33.glBindVertexArray(currentVAO)
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)

        NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear()
    }

    private fun transformSkinning(stack: PoseStack) {
        GL33.glUseProgram(GltfManager.glProgramSkinning)
        GL33.glEnable(GL33.GL_RASTERIZER_DISCARD)
        modelTree.scenes.forEach { it.transformSkinning(stack) }
        GL33.glBindBuffer(GL33.GL_TEXTURE_BUFFER, 0)
        GL33.glDisable(GL33.GL_RASTERIZER_DISCARD)
    }

    fun destroy() {
        modelTree.walkNodes().mapNotNull { it.mesh }.flatMap { it.primitives }.forEach(GltfTree.Primitive::destroy)
    }
}

private fun Vector4f.isNotEmpty(): Boolean {
    return x() != 0f || y() != 0f || z() != 0f || w() != 0f
}
