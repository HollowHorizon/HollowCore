package ru.hollowhorizon.hc.client.screens.widget.button

import com.mojang.blaze3d.matrix.MatrixStack
import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.widget.button.Button
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.client.utils.drawCentredScaled
import ru.hollowhorizon.hc.client.utils.drawScaled
import ru.hollowhorizon.hc.client.utils.mc
import javax.annotation.Nonnull


open class BaseButton @JvmOverloads constructor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    protected val text: ITextComponent = StringTextComponent(""),
    private val pressable: BasePressable,
    private val texLocation: ResourceLocation,
    protected val textColor: Int = 0xFFFFFF,
    protected val textColorHovered: Int = 0xF0F0F0,
    protected val tooltip: ITextComponent = StringTextComponent(""),
    protected val textScale: Float = 1.0F,
) : Button(x, y, width, height, text, { }) {
    var isClickable = true

    override fun render(@Nonnull stack: MatrixStack, x: Int, y: Int, f: Float) {
        val minecraft = Minecraft.getInstance()
        val fr = minecraft.font
        val isHovered = isCursorAtButton(x, y)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.defaultAlphaFunc()
        stack.pushPose()
        stack.translate(0.0, 0.0, 700.0)

        minecraft.getTextureManager().bind(texLocation)
        blit(
            stack, this.x, this.y, 0f, (if (isHovered) height else 0).toFloat(), width, height, width, height * 2
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

    override fun onPress() {
        if(isClickable) pressable.onPress(this)
    }

    fun isCursorAtButton(cursorX: Int, cursorY: Int): Boolean {
        return cursorX >= x && cursorY >= y && cursorX <= x + width && cursorY <= y + height && isClickable
    }

    @OnlyIn(Dist.CLIENT)
    fun interface BasePressable {
        fun onPress(button: Button)
    }
}