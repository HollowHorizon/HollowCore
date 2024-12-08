package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import de.fabmax.kool.pipeline.*
import de.fabmax.kool.pipeline.backend.gl.*
import de.fabmax.kool.pipeline.backend.stats.BackendStats
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.scene.addColorMesh
import de.fabmax.kool.util.Time
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.client.imgui.imguiWindowBuffer

val SKY = KoolDrawer {
    mcCamera()

    addColorMesh {
        generate {
            cube {
                colored()
            }
        }
    }
}

open class KoolDrawer(builder: Scene.() -> Unit) {
    private var prevFrameTime = 0L
    private var oldLine: Float = 1f
    val scene = Scene("Ingame Drawer").apply(builder)
    private val timeQuery: TimeQuery by lazy { TimeQuery(MCGlApi) }

    fun draw() {
        val oldVAO = GL30.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING)
        val oldEBO = GL30.glGetInteger(GL30.GL_ELEMENT_ARRAY_BUFFER_BINDING)
        val oldBuffer = GL30.glGetInteger(GL30.GL_ARRAY_BUFFER_BINDING)

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

        if (scene.isVisible) scene.renderScene(KoolManager.ctx)

        val t = Time.precisionTime
        val scenePass = scene.mainRenderPass
        renderViews(scenePass)
        scenePass.afterDraw()
        scene.sceneDrawTime = Time.precisionTime - t

        GL30.glBindVertexArray(oldVAO)
        GL30.glBindBuffer(GL30.GL_ELEMENT_ARRAY_BUFFER, oldEBO)
        GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, oldBuffer)
    }

    private fun doOffscreenPasses(scene: Scene) {
        val passes = KoolHooks.renderPasses(scene)
        for (i in passes.indices) {
            val pass = passes[i]
            if (pass.isEnabled) {
                drawOffscreen(pass)
                pass.afterDraw()
            }
        }
    }

    private fun drawOffscreen(offscreenPass: OffscreenRenderPass) {
        when (offscreenPass) {
            is OffscreenRenderPass2d -> KoolHooks.getImpl(offscreenPass).draw()
            is OffscreenRenderPassCube -> KoolHooks.getImpl(offscreenPass).draw()
            is ComputeRenderPass -> KoolHooks.getImpl(offscreenPass).dispatch()
            is OffscreenRenderPass2dPingPong -> drawOffscreenPingPong(offscreenPass)
            else -> throw IllegalArgumentException("Offscreen pass type not implemented: $offscreenPass")
        }
    }

    protected fun OffscreenPass2dImpl.draw() = (this as OffscreenRenderPass2dGl).draw()
    protected fun OffscreenPassCubeImpl.draw() = (this as OffscreenRenderPassCubeGl).draw()
    protected fun ComputePassImpl.dispatch() = (this as ComputeRenderPassGl).dispatch()

    private fun drawOffscreenPingPong(offscreenPass: OffscreenRenderPass2dPingPong) {
        for (i in 0 until offscreenPass.pingPongPasses) {
            offscreenPass.onDrawPing?.invoke(i)
            KoolHooks.getImpl(offscreenPass.ping).draw()
            offscreenPass.onDrawPong?.invoke(i)
            KoolHooks.getImpl(offscreenPass.pong).draw()
        }
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