/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.HollowCore.MODID


class SwitchWidget(x: Int, y: Int, w: Int, h: Int, val onChange: (Boolean) -> Unit) :
    HollowWidget(x, y, w, h, Component.empty()) {
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