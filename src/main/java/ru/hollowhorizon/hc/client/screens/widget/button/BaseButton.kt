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
import ru.hollowhorizon.hc.client.utils.mc
import javax.annotation.Nonnull

open class BaseButton(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    private val text: ITextComponent = StringTextComponent(""),
    private val pressable: BasePressable,
    private val texLocation: ResourceLocation,
    private val textColor: Int = 0xFFFFFF,
    private val textColorHovered: Int = 0xF0F0F0,
    private val tooltip: ITextComponent = StringTextComponent(""),
) : Button(x, y, width, height, text, null) {

    override fun render(@Nonnull stack: MatrixStack, x: Int, y: Int, f: Float) {
        val minecraft = Minecraft.getInstance()
        val fr = minecraft.font
        val isHovered = isCursorAtButton(x, y)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.defaultAlphaFunc()
        stack.pushPose()

        minecraft.getTextureManager().bind(texLocation)
        blit(
            stack, this.x, this.y, 0f, (if (isHovered) height else 0).toFloat(), width, height, width, height * 2
        )

        fr.draw(
            stack,
            text,
            this.x + width / 2f - fr.width(text) / 2f,
            this.y + height / 4f,
            if (isHovered) textColorHovered else textColor
        )

        stack.popPose()

        if (mc.screen != null && isHovered) {
            mc.screen!!.renderTooltip(stack, minecraft.font.split(tooltip, (width / 2 - 43).coerceAtLeast(170)), x, y)
        }
    }

    override fun onPress() {
        pressable.onPress(this)
    }

    private fun isCursorAtButton(cursorX: Int, cursorY: Int): Boolean {
        return cursorX >= x && cursorY >= y && cursorX <= x + width && cursorY <= y + height
    }

    @OnlyIn(Dist.CLIENT)
    interface BasePressable {
        fun onPress(p_onPress_1_: Button)
    }
}