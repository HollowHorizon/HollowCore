package ru.hollowhorizon.hc.common.ui.widgets

import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.api.utils.Polymorphic
import ru.hollowhorizon.hc.client.utils.drawScaled
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.nbt.ForTextComponent
import ru.hollowhorizon.hc.common.ui.Anchor
import ru.hollowhorizon.hc.common.ui.IWidget
import ru.hollowhorizon.hc.common.ui.Widget

@Serializable
@Polymorphic(IWidget::class)
class LabelWidget(
    val text: @Serializable(ForTextComponent::class) Component,
    var anchor: Anchor = Anchor.CENTER,
    var color: Int = 0xFFFFFF,
    var scale: Float = 1f,
) : Widget() {
    var textOffsetX = 0
    var textOffsetY = 0

    init {
        width = text.string.length.px
        height = 9.px
    }

    @OnlyIn(Dist.CLIENT)
    override fun renderWidget(
        stack: PoseStack,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
        widgetWidth: Int,
        widgetHeight: Int,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        Minecraft.getInstance().font.let { font ->
            val lines = font.split(text, (widgetWidth / scale).toInt())
            val height = lines.size * (font.lineHeight * scale).toInt()
            lines.forEachIndexed { i, line ->
                val realWidth = (anchor.factor * widgetWidth).toInt()
                val realHeight = widgetHeight / 2 - height / 2 + (font.lineHeight / 2 * scale).toInt()
                font.drawScaled(stack, anchor, line, x + realWidth + textOffsetX, y + realHeight + i * (font.lineHeight * scale).toInt() + textOffsetY, color, scale)
            }
        }
    }
}

fun Widget.label(text: MutableComponent, config: LabelWidget.() -> Unit) {
    this += LabelWidget(text).apply(config)
}

fun Widget.label(text: String, config: LabelWidget.() -> Unit) {
    this += LabelWidget(text.mcText).apply(config)
}