package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.utils.mcText


class SwitchWidget(x: Int, y: Int, w: Int, h: Int, val onChange: (Boolean) -> Unit) :
    HollowWidget(x, y, w, h, "".mcText) {
    var value: Boolean = false
    private var processAnim = false
    private var processCounter = 0

    override fun render(stack: PoseStack, mouseX: Int, mouseY: Int, particalTick: Float) {
        this.isHovered = mouseX in x..x + width && mouseY in y..y + height

        RenderSystem.setShaderTexture(0, SLIDER_BASE)
        blit(stack, this.x, this.y, 0f, 0f, this.width, this.height, this.width, this.height * 3)

        stack.pushPose()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, processCounter / 10f)

        blit(
            stack, this.x, this.y, 0f, (this.height * 2).toFloat(),
            this.width, this.height, this.width, this.height * 3
        )

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1f)
        RenderSystem.disableBlend()
        stack.popPose()

        val progress = if (processAnim) (processCounter + particalTick) / 10f else processCounter / 10f
        blit(
            stack, this.x + (0.6667f * this.width * progress).toInt(), this.y, 0f,
            height.toFloat(), this.width, this.height, this.width, this.height * 3
        )
    }

    override fun tick() {
        super.tick()

        if (this.processAnim) {
            if (this.value) {
                if (processCounter < 10) processCounter += 2
                else this.processAnim = false
            } else {
                if (processCounter > 0) processCounter += 2
                else this.processAnim = false
            }
        }
    }

    override fun mouseClicked(p_231044_1_: Double, p_231044_3_: Double, button: Int): Boolean {
        if (this.isHovered && button == 0) {
            this.value = !this.value
            onChange(this.value)
            this.processAnim = true
            //Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(HSSounds.SLIDER_BUTTON, 1.0F));
            return true
        }
        return false
    }

    companion object {
        val SLIDER_BASE: ResourceLocation = ResourceLocation(MODID, "textures/gui/slider_base.png")
    }
}