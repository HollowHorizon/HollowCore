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
import net.minecraft.world.entity.Pose
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
        consumer: (ResourceLocation) -> Int,
        light: Int,
        overlay: Int,
    ) {
        val activeTexture = GlStateManager._getActiveTexture()

        modelTree.scenes.forEach {
            it.nodes.forEach { node ->
                node.renderDecorations(stack, visuals, modelData, light)
            }
        }

        CURRENT_NORMAL = stack.last().normal()

        transformSkinning(stack)

        //Получение текущих VAO и IBO
        val currentVAO = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING)
        val currentElementArrayBuffer = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING)

        GL33.glVertexAttrib4f(1, 1.0F, 1.0F, 1.0F, 1.0F) // Цвет
        GL33.glVertexAttribI2i(3, overlay and '\uffff'.code, overlay shr 16 and '\uffff'.code) // Оверлей при ударе
        GL33.glVertexAttribI2i(4, light and '\uffff'.code, light shr 16 and '\uffff'.code) // Освещение

        GlStateManager._activeTexture(GL33.GL_TEXTURE2)
        GlStateManager._bindTexture(GltfManager.lightTexture.id)
        GlStateManager._activeTexture(GL33.GL_TEXTURE1)
        Minecraft.getInstance().gameRenderer.overlayTexture().setupOverlayColor()
        GlStateManager._bindTexture(RenderSystem.getShaderTexture(1))
        Minecraft.getInstance().gameRenderer.overlayTexture().teardownOverlayColor()
        GlStateManager._activeTexture(GL33.GL_TEXTURE0)
        val texture = GlStateManager._getTextureId(GlStateManager._getActiveTexture())

        drawWithShader(if (!areShadersEnabled) ModShaders.GLTF_ENTITY else GameRenderer.getRendertypeEntityTranslucentShader()!!) {
            modelTree.scenes.forEach { it.render(stack, visuals, modelData, consumer, light) }
        }

        GlStateManager._bindTexture(texture)
        GlStateManager._activeTexture(activeTexture)

        GL33.glBindVertexArray(currentVAO)
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)
        GL33.glUseProgram(0)

        NODE_GLOBAL_TRANSFORMATION_LOOKUP_CACHE.clear()
    }

    private fun transformSkinning(stack: PoseStack) {
        GL33.glUseProgram(GltfManager.glProgramSkinning)
        GL33.glEnable(GL33.GL_RASTERIZER_DISCARD)
        modelTree.scenes.forEach { it.transformSkinning(stack) }
        GL33.glBindBuffer(GL33.GL_TEXTURE_BUFFER, 0)
        GL33.glDisable(GL33.GL_RASTERIZER_DISCARD)
    }

    fun pickColor(stack: PoseStack, mouseX: Double, mouseY: Double) {
        transformSkinning(stack)

        ModShaders.GLTF_ENTITY_COLOR_PICK.apply()
        GL33.glEnable(GL33.GL_RASTERIZER_DISCARD)
        modelTree.scenes.forEach { it.pickColor(stack, mouseX, mouseY) }
        GL33.glDisable(GL33.GL_RASTERIZER_DISCARD)
        ModShaders.GLTF_ENTITY_COLOR_PICK.clear()
    }

    fun destroy() {
        modelTree.walkNodes().mapNotNull { it.mesh }.flatMap { it.primitives }.forEach(GltfTree.Primitive::destroy)
    }
}

private fun Vector4f.isNotEmpty(): Boolean {
    return x() != 0f || y() != 0f || z() != 0f || w() != 0f
}
