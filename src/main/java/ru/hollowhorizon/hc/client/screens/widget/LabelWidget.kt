package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.utils.drawScaled
import ru.hollowhorizon.hc.common.ui.Anchor
import kotlin.math.max

private val font get() = Minecraft.getInstance().font

class LabelWidget(
    text: Component,
    private val hovered: Component = text,
    private val anchor: Anchor = Anchor.CENTER,
    private val color: Int,
    private val hoveredColor: Int,
    val scale: Float = 1f,
) : HollowWidget(
    0, 0,
    (max(font.width(text), font.width(hovered)) * scale).toInt(), (9 * scale).toInt(), text
) {
    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        super.renderButton(stack, mouseX, mouseY, ticks)
        font.drawScaled(
            stack, anchor, if (isHovered) message else hovered, x, y, if (isHovered) color else hoveredColor, scale
        )
    }
}