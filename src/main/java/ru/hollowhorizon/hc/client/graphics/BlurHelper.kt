package ru.hollowhorizon.hc.client.graphics

import com.google.common.collect.Queues
import com.mojang.blaze3d.pipeline.RenderCall
import com.mojang.blaze3d.pipeline.RenderTarget
import com.mojang.blaze3d.pipeline.TextureTarget
import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.client.utils.KernelUtil
import ru.hollowhorizon.hc.client.utils.mc


object BlurHelper {
    private val blur = Shader("blur.frag")
    private val renderQueue = Queues.newConcurrentLinkedQueue<RenderCall>()
    private val window = Minecraft.getInstance().window
    private val inFrameBuffer = TextureTarget(window.width / 2, window.height / 2, true, Minecraft.ON_OSX)
    private val outFrameBuffer = TextureTarget(window.width / 2, window.height / 2, true, Minecraft.ON_OSX)

    @JvmStatic
    fun registerRenderCall(rc: RenderCall) {
        renderQueue.add(rc)
    }

    @JvmStatic
    fun draw(radius: Int) {
        if (renderQueue.isEmpty()) return
        setupBuffer(inFrameBuffer)
        setupBuffer(outFrameBuffer)
        inFrameBuffer.bindWrite(true)
        while (!renderQueue.isEmpty()) {
            renderQueue.poll().execute()
        }
        outFrameBuffer.bindWrite(true)
        blur.load()
        blur.setUniformf("radius", radius.toFloat())
        blur.setUniformi("sampler1", 0)
        blur.setUniformi("sampler2", 20)
        blur.setUniformfb("kernel", KernelUtil.loadKernel(radius))
        blur.setUniformf("texelSize", 1.0f / window.width, 1.0f / window.height)
        blur.setUniformf("direction", 2.0f, 0.0f)
        GlStateManager._disableBlend()
        GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO)
        mc.mainRenderTarget.bindRead()
        Shader.draw()
        mc.mainRenderTarget.bindWrite(true)
        blur.setUniformf("direction", 0.0f, 2.0f)
        outFrameBuffer.bindRead()
        GL30.glActiveTexture(GL30.GL_TEXTURE20)
        inFrameBuffer.bindRead()
        GL30.glActiveTexture(GL30.GL_TEXTURE0)
        Shader.draw()
        blur.unload()
        inFrameBuffer.unbindRead()
        GlStateManager._disableBlend()
    }

    private fun setupBuffer(frameBuffer: RenderTarget): RenderTarget {
        if (frameBuffer.width != window.width / 2 || frameBuffer.height != window.height / 2) frameBuffer.resize(
            window.width / 2,
            window.height / 2,
            Minecraft.ON_OSX
        ) else frameBuffer.clear(Minecraft.ON_OSX)
        return frameBuffer
    }
}