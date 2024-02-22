package ru.hollowhorizon.hc.client.screens.widget.button

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
import ru.hollowhorizon.hc.client.utils.drawScaled
import ru.hollowhorizon.hc.client.utils.mc
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.common.ui.Anchor
import javax.annotation.Nonnull


open class BaseButton @JvmOverloads constructor(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    protected val text: Component = "".mcText,
    private val pressable: BaseButton.() -> Unit,
    private val texLocation: ResourceLocation,
    protected val textColor: Int = 0xFFFFFF,
    protected val textColorHovered: Int = 0xF0F0F0,
    protected val tooltip: Component = "".mcText,
    protected val textScale: Float = 1.0F,
) : HollowWidget(x, y, width, height, text) {
    var isClickable = true

    override fun render(@Nonnull stack: PoseStack, x: Int, y: Int, f: Float) {
        val minecraft = Minecraft.getInstance()
        val fr = minecraft.font
        val isHovered = isCursorAtButton(x, y)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()

        stack.pushPose()
        stack.translate(0.0, 0.0, 700.0)

        RenderSystem.setShaderTexture(0, texLocation)
        blit(
            stack, this.x, this.y, 0f, (if (isHovered) height else 0).toFloat(), width, height, width, height * 2
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

    fun onPress() {
        if (isClickable) pressable.invoke(this)
    }

    open fun isCursorAtButton(cursorX: Int, cursorY: Int): Boolean {
        return cursorX >= x && cursorY >= y && cursorX <= x + width && cursorY <= y + height && isClickable
    }

    override fun clicked(pMouseX: Double, pMouseY: Double): Boolean {
        return super.clicked(pMouseX, pMouseY).apply {
            if (this) onPress()
        }
    }
}