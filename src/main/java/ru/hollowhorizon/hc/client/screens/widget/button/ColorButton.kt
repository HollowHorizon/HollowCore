package ru.hollowhorizon.hc.client.screens.widget.button

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import ru.hollowhorizon.hc.client.utils.drawCentredScaled
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.toRL

class ColorButton @JvmOverloads constructor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    text: ITextComponent = StringTextComponent(""),
    pressable: BasePressable,
    private val buttonColor: Int,
    private val buttonColorHovered: Int = buttonColor,
    textColor: Int = 0xFFFFFF,
    textColorHovered: Int = 0xF0F0F0,
    tooltip: ITextComponent = StringTextComponent(""),
    textScale: Float = 1.0F,
) : BaseButton(x, y, width, height, text, pressable, "".toRL(), textColor, textColorHovered, tooltip, textScale) {

    override fun render(stack: MatrixStack, x: Int, y: Int, f: Float) {
        val minecraft = Minecraft.getInstance()
        val fr = minecraft.font
        val isHovered = isCursorAtButton(x, y)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.defaultAlphaFunc()
        stack.pushPose()

        fill(
            stack, this.x, this.y, this.x + width, this.y + height,
            if (isHovered) buttonColorHovered
            else buttonColor
        )

        fr.drawCentredScaled(
            stack, text,
            this.x + width / 2, this.y + height / 2,
            if (isHovered) textColorHovered else textColor, textScale
        )

        stack.popPose()

        if (mc.screen != null && isHovered && tooltip.string != "") {
            mc.screen!!.renderTooltip(stack, minecraft.font.split(tooltip, (width / 2 - 43).coerceAtLeast(170)), x, y)
        }
    }
}