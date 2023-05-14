package ru.hollowhorizon.hc.client.models

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.renderer.IRenderTypeBuffer
import ru.hollowhorizon.hc.client.utils.use

class HollowModelRenderer(val model: HollowModel) {
    fun render(buffers: IRenderTypeBuffer, stack: MatrixStack, partialTicks: Float, i: Int) {
        RenderSystem.enableAlphaTest()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        stack.use {
            stack.scale(0.01f, 0.01f, 0.01f)
            model.root.render(buffers, model, stack, i)
        }
        RenderSystem.disableBlend()
        RenderSystem.disableAlphaTest()
    }
}