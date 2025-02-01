package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.platform.GlStateManager
import de.fabmax.kool.pipeline.CullMethod
import de.fabmax.kool.pipeline.DepthCompareOp
import de.fabmax.kool.pipeline.backend.gl.GlRenderPass
import de.fabmax.kool.pipeline.backend.gl.glOp
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL33

object KoolDrawer {
    var actIsWriteDepth = true
    var actDepthTest: DepthCompareOp = DepthCompareOp.LESS_EQUAL
    var actCullMethod: CullMethod = CullMethod.NO_CULLING
    var lineWidth = 1f

    fun drawOverlays() {
        val scenes = KoolManager.context.scenes
        scenes.forEach { it.isVisible = it is ScreenScene }
        draw()
        scenes.forEach { it.isVisible = it !is ScreenScene }
    }

    fun draw() {
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
            MCGlApi.depthFunc(actDepthTest.glOp(MCGlApi))
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

    private val currentDepthOp
        get() = when (GL33.glGetInteger(GL33.GL_DEPTH_FUNC)) {
            GL33.GL_ALWAYS -> DepthCompareOp.ALWAYS
            GL33.GL_NEVER -> DepthCompareOp.NEVER
            GL33.GL_LESS -> DepthCompareOp.LESS
            GL33.GL_LEQUAL -> DepthCompareOp.LESS_EQUAL
            GL33.GL_GREATER -> DepthCompareOp.GREATER
            GL33.GL_GEQUAL -> DepthCompareOp.GREATER_EQUAL
            GL33.GL_EQUAL -> DepthCompareOp.EQUAL
            GL33.GL_NOTEQUAL -> DepthCompareOp.NOT_EQUAL
            else -> throw IllegalStateException("Unknown depth compare operation")
        }

    private val currentCull: CullMethod
        get() {
            if (!GL33.glIsEnabled(GL33.GL_CULL_FACE)) return CullMethod.NO_CULLING
            return when (GL33.glGetInteger(GL33.GL_CULL_FACE_MODE)) {
                GL33.GL_BACK -> CullMethod.CULL_BACK_FACES
                GL33.GL_FRONT -> CullMethod.CULL_FRONT_FACES
                else -> CullMethod.NO_CULLING // На случай необычного значения.
            }
        }
}