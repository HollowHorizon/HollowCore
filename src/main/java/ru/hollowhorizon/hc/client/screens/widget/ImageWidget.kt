package ru.hollowhorizon.hc.client.screens.widget


import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.sounds.SoundManager
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.toSTC

class ImageWidget(val location: ResourceLocation, x: Int, y: Int, width: Int, height: Int) :
    HollowWidget(x, y, width, height, "IMAGE_WIDGET".toSTC()) {

    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        bind(location)
        blit(stack, x, y, 0F, 0F, width, height, width, height)
        super.renderButton(stack, mouseX, mouseY, ticks)
    }

    override fun playDownSound(p_230988_1_: SoundManager) {
        // NO-OP
    }
}
