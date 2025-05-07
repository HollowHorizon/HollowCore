package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import de.fabmax.kool.pipeline.CullMethod
import de.fabmax.kool.pipeline.DepthCompareOp
import de.fabmax.kool.pipeline.backend.gl.GlRenderPass
import de.fabmax.kool.pipeline.backend.gl.glOp
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.client.kool.gl.MCGlApi

object KoolDrawer {
    fun drawOverlays() {
        val scenes = KoolManager.context.scenes
        scenes.forEach { it.isVisible = it.isScreenScene() }
        guiFramebuffer.clear(Minecraft.ON_OSX)
        draw(true)
        scenes.forEach { it.isVisible = !it.isScreenScene() }
    }

    fun draw(isScreenPass: Boolean = false) {
        MCGlApi.clipControl(MCGlApi.LOWER_LEFT, MCGlApi.NEGATIVE_ONE_TO_ONE)
        val activeTexture = GlStateManager._getActiveTexture()
        val currentTexture = GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D)
        val currentVAO = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING)
        val currentElementArrayBuffer = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING)

        MCGlApi.depthMask(GlRenderPass.GlState.actIsWriteDepth)
        if (GlRenderPass.GlState.actDepthTest == DepthCompareOp.ALWAYS) {
            MCGlApi.disable(MCGlApi.DEPTH_TEST)
        } else {
            MCGlApi.enable(MCGlApi.DEPTH_TEST)
            GlRenderPass.GlState.actDepthTest?.glOp(MCGlApi)?.let(MCGlApi::depthFunc)
        }
        when (GlRenderPass.GlState.actCullMethod) {
            CullMethod.CULL_BACK_FACES -> {
                MCGlApi.enable(MCGlApi.CULL_FACE)
                MCGlApi.cullFace(MCGlApi.BACK)
            }

            CullMethod.CULL_FRONT_FACES -> {
                MCGlApi.enable(MCGlApi.CULL_FACE)
                MCGlApi.cullFace(MCGlApi.FRONT)
            }

            else -> MCGlApi.disable(MCGlApi.CULL_FACE)
        }
        if(GlRenderPass.GlState.lineWidth != 0f) MCGlApi.lineWidth(GlRenderPass.GlState.lineWidth)
        if(!isScreenPass) {
            MCGlApi.enable(MCGlApi.DEPTH_TEST)
            MCGlApi.depthFunc(MCGlApi.LEQUAL)
        } else {
            MCGlApi.disable(MCGlApi.DEPTH_TEST)
        }

        KoolManager.context.renderFrame()

        GL33.glActiveTexture(activeTexture)
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, currentTexture)
        GL33.glBindVertexArray(currentVAO)
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)

        GL33.glEnable(GL33.GL_DEPTH_TEST)
        GL33.glDepthFunc(GL33.GL_LEQUAL)
        GL33.glEnable(GL33.GL_BLEND)
        GL33.glBlendFuncSeparate(
            GL33.GL_SRC_ALPHA,
            GL33.GL_ONE_MINUS_SRC_ALPHA,
            GL33.GL_ONE,
            GL33.GL_ONE_MINUS_SRC_ALPHA
        )
        GL33.glCullFace(GL33.GL_BACK)
        GL33.glEnable(GL33.GL_CULL_FACE)

        Minecraft.getInstance().mainRenderTarget.bindWrite(true)
    }
}