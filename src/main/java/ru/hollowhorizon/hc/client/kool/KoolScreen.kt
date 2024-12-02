package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import de.fabmax.kool.pipeline.*
import de.fabmax.kool.pipeline.backend.gl.TimeQuery
import de.fabmax.kool.pipeline.backend.gl.glOp
import de.fabmax.kool.pipeline.backend.stats.BackendStats
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.util.Time
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.client.imgui.WINDOW_BUFFER
import ru.hollowhorizon.hc.client.imgui.imguiWindowBuffer
import ru.hollowhorizon.hc.client.utils.literal

class KoolScreen(builder: Scene.() -> Unit) : Screen("".literal) {
    private var prevFrameTime = 0L
    private var oldLine: Float = 1f
    val scene = Scene(title.string).apply(builder)
    private val timeQuery: TimeQuery by lazy { TimeQuery(MCGlApi) }

    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics)

        imguiWindowBuffer.clear(Minecraft.ON_OSX)
        Minecraft.getInstance().mainRenderTarget.bindWrite(true)
        BackendStats.resetPerFrameCounts()
        KoolHooks.resetShaders(KoolManager.ctx)
        KoolHooks.executeCoroutineTasks()

        // determine time delta
        val time = System.nanoTime()
        val dt = (time - prevFrameTime) / 1e9
        prevFrameTime = time

        // setup draw queues for all scenes / render passes
        KoolManager.ctx.callRender(dt)
        if(scene.isVisible) scene.renderScene(KoolManager.ctx)

        val t = Time.precisionTime
        val scenePass = scene.mainRenderPass
        renderViews(scenePass)
        scenePass.afterDraw()
        scene.sceneDrawTime = Time.precisionTime - t
    }

    protected fun renderViews(renderPass: RenderPass) {
        val q = if (renderPass.isProfileTimes) timeQuery else null
        q?.let {
            if (it.isAvailable) {
                renderPass.tGpu = it.getQueryResultMillis()
            }
            it.begin()
        }

        for (mipLevel in 0 until renderPass.numRenderMipLevels) {
            renderPass.setupMipLevel(mipLevel)

            when (renderPass.viewRenderMode) {
                RenderPass.ViewRenderMode.SINGLE_RENDER_PASS -> {
                    for (viewIndex in renderPass.views.indices) {
                        val view = renderPass.views[viewIndex]
                        view.setupView()
                        renderView(view, viewIndex, mipLevel)
                    }
                }

                RenderPass.ViewRenderMode.MULTI_RENDER_PASS -> {
                    for (viewIndex in renderPass.views.indices) {
                        val view = renderPass.views[viewIndex]
                        view.setupView()
                        renderView(view, viewIndex, mipLevel)
                    }
                }
            }
        }

        q?.end()
    }

    override fun onClose() {
        super.onClose()
        timeQuery.release()
    }

    protected fun renderView(view: RenderPass.View, viewIndex: Int, mipLevel: Int) {
        view.drawQueue.forEach { cmd ->
            if (cmd.isActive) {
                val drawInfo = KoolHooks.shaderManager(KoolManager.ctx).bindDrawShader(cmd)
                val isValid = cmd.geometry.numIndices > 0 && drawInfo.isValid && drawInfo.numIndices > 0

                if (isValid) {
                    val pipeline = cmd.pipeline
                    val isReversedDepth = view.renderPass.isReverseDepth

                    setBlendMode(pipeline.blendMode)
                    setDepthTest(pipeline, isReversedDepth)
                    setWriteDepth(pipeline.isWriteDepth)
                    setCullMethod(pipeline.cullMethod)
                    if (pipeline.lineWidth != oldLine) {
                        oldLine = pipeline.lineWidth
                        GL30.glLineWidth(pipeline.lineWidth)
                    }

                    val insts = cmd.instances
                    if (insts == null) {
                        MCGlApi.drawElements(drawInfo.primitiveType, drawInfo.numIndices, drawInfo.indexType)
                        BackendStats.addDrawCommands(1, cmd.geometry.numPrimitives)
                    } else if (insts.numInstances > 0) {
                        MCGlApi.drawElementsInstanced(
                            drawInfo.primitiveType,
                            drawInfo.numIndices,
                            drawInfo.indexType,
                            insts.numInstances
                        )
                        BackendStats.addDrawCommands(1, cmd.geometry.numPrimitives * insts.numInstances)
                    }
                }
            }
        }
    }

    private fun setCullMethod(cullMethod: CullMethod) {
        when (cullMethod) {
            CullMethod.CULL_BACK_FACES -> {
                RenderSystem.enableCull()
                MCGlApi.cullFace(MCGlApi.BACK)
            }

            CullMethod.CULL_FRONT_FACES -> {
                RenderSystem.enableCull()
                MCGlApi.cullFace(MCGlApi.FRONT)
            }

            CullMethod.NO_CULLING -> RenderSystem.disableCull()
        }

    }

    fun setWriteDepth(enabled: Boolean) {
        RenderSystem.depthMask(enabled)
    }

    fun setDepthTest(pipeline: DrawPipeline, isReversedDepth: Boolean) {
        val depthCompareOp = if (isReversedDepth && pipeline.autoReverseDepthFunc) {
            when (pipeline.depthCompareOp) {
                DepthCompareOp.LESS -> DepthCompareOp.GREATER
                DepthCompareOp.LESS_EQUAL -> DepthCompareOp.GREATER_EQUAL
                DepthCompareOp.GREATER -> DepthCompareOp.LESS
                DepthCompareOp.GREATER_EQUAL -> DepthCompareOp.LESS_EQUAL
                else -> pipeline.depthCompareOp
            }
        } else {
            pipeline.depthCompareOp
        }

        if (depthCompareOp == DepthCompareOp.ALWAYS && !pipeline.isWriteDepth) {
            RenderSystem.disableDepthTest()
        } else {
            RenderSystem.enableDepthTest()
            RenderSystem.depthFunc(depthCompareOp.glOp(MCGlApi))
        }

    }

    private fun setBlendMode(blendMode: BlendMode) {
        when (blendMode) {
            BlendMode.DISABLED -> RenderSystem.disableBlend()
            BlendMode.BLEND_ADDITIVE -> {
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE)
                RenderSystem.enableBlend()
            }

            BlendMode.BLEND_MULTIPLY_ALPHA -> {
                RenderSystem.blendFunc(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                )
                RenderSystem.enableBlend()
            }

            BlendMode.BLEND_PREMULTIPLIED_ALPHA -> {
                RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
                RenderSystem.enableBlend()
            }
        }
    }

    private fun RenderPass.View.setupView() {
        onSetupView.update()
        for (i in onSetupView.indices) {
            onSetupView[i]()
        }
    }
}