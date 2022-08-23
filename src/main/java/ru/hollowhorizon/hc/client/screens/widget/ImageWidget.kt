package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.audio.SoundHandler
import net.minecraft.util.ResourceLocation
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.toSTC

class ImageWidget(val location: ResourceLocation, x: Int, y: Int, width: Int, height: Int) :
    HollowWidget(x, y, width, height, "IMAGE_WIDGET".toSTC()) {

    override fun renderButton(stack: MatrixStack, mouseX: Int, mouseY: Int, ticks: Float) {
        mc.textureManager.bind(location)
        blit(stack, x, y, 0F, 0F, width, height, width, height)
        super.renderButton(stack, mouseX, mouseY, ticks)
    }

    override fun playDownSound(p_230988_1_: SoundHandler) {
        // NO-OP
    }

}