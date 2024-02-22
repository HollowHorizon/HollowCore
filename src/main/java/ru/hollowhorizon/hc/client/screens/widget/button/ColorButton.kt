package ru.hollowhorizon.hc.client.screens.widget.button

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.utils.drawScaled
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.ui.Anchor

class ColorButton @JvmOverloads constructor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    text: Component = "".mcText,
    pressable: BaseButton.() -> Unit,
    private val buttonColor: Int,
    private val buttonColorHovered: Int = buttonColor,
    textColor: Int = 0xFFFFFF,
    textColorHovered: Int = 0xF0F0F0,
    tooltip: Component = "".mcText,
    textScale: Float = 1.0F,
) : BaseButton(x, y, width, height, text, pressable, "".rl, textColor, textColorHovered, tooltip, textScale) {

    override fun render(stack: PoseStack, x: Int, y: Int, f: Float) {
        val minecraft = Minecraft.getInstance()
        val fr = minecraft.font
        val isHovered = isCursorAtButton(x, y)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        stack.pushPose()

        fill(
            stack, this.x, this.y, this.x + width, this.y + height,
            if (isHovered) buttonColorHovered
            else buttonColor
        )

        fr.drawScaled(
            stack, Anchor.CENTER, text,
            this.x + width / 2, this.y + height / 2,
            if (isHovered) textColorHovered else textColor, textScale
        )

        stack.popPose()

        if (mc.screen != null && isHovered && tooltip.string != "") {
            mc.screen!!.renderTooltip(stack, minecraft.font.split(tooltip, (width / 2 - 43).coerceAtLeast(170)), x, y)
        }
    }
}