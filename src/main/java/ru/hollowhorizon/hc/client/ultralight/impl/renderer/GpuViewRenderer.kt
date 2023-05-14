package ru.hollowhorizon.hc.client.ultralight.impl.renderer

import com.labymedia.ultralight.UltralightView
import com.labymedia.ultralight.config.UltralightViewConfig
import com.labymedia.ultralight.gpu.UltralightOpenGLGPUDriverNative
import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.gui.AbstractGui
import ru.hollowhorizon.hc.client.utils.mc

/**
 * A gpu view renderer
 */
class GpuViewRenderer(val driver: UltralightOpenGLGPUDriverNative) : ViewRenderer {

    var window = 0L

    override fun setupConfig(viewConfig: UltralightViewConfig) {
        viewConfig.isAccelerated(true)
        
        window = mc.window.window
    }

    override fun render(view: UltralightView, matrices: MatrixStack) {
        driver.setActiveWindow(window)

        RenderSystem.clearColor(0f, 0f, 0f, 0f)

        if (driver.hasCommandsPending()) {
            driver.drawCommandList()
        }

        RenderSystem.clearColor(0f, 1f, 0f, 1f)

        val renderTarget = view.renderTarget()
        val textureId = renderTarget.textureId

        driver.bindTexture(0, textureId)
        AbstractGui.blit(
            matrices,
            0,
            0,
            0f,
            0f,
            mc.window.guiScaledWidth,
            mc.window.guiScaledHeight,
            mc.window.guiScaledWidth,
            mc.window.guiScaledHeight
        )
    }

    override fun delete() {
    }

}
