package ru.hollowhorizon.hc.common.ui.widgets

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.resources.ResourceLocation
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.player.Player
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.api.utils.Polymorphic
import ru.hollowhorizon.hc.client.utils.drawScaled
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.nbt.ForResourceLocation
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hc.common.ui.*

@Serializable
@Polymorphic(IWidget::class)
class ButtonWidget(val text: String, val image: @Serializable(ForResourceLocation::class) ResourceLocation) : Widget() {
    var anchor = Anchor.CENTER
    var hoverText = text
    var color = 0xFFFFFF
    var hoverColor = 0xFFFFFF
    var scale = 1.5f

    @Transient
    var onClick = {}

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
        val hovered = mouseX in x..x + widgetWidth && mouseY in y..y + widgetHeight
        val text = (if (hovered) hoverText else text).mcText
        RenderSystem.enableBlend()
        RenderSystem.setShaderTexture(0, image)
        Screen.blit(
            stack,
            x,
            y,
            0f,
            if (hovered) widgetHeight.toFloat() else 0f,
            widgetWidth,
            widgetHeight,
            widgetWidth,
            widgetHeight * 2
        )
        RenderSystem.disableBlend()
        Minecraft.getInstance().font.let { font ->
            val lines = font.split(text, (widgetWidth / scale).toInt())
            val height = lines.size * (font.lineHeight * scale).toInt()
            lines.forEachIndexed { i, line ->
                val realWidth = (anchor.factor * widgetWidth).toInt()
                val realHeight = widgetHeight / 2 - height / 2 + (font.lineHeight / 2 * scale).toInt()
                font.drawScaled(stack, anchor, line, x + realWidth, y + realHeight + i * (font.lineHeight * scale).toInt(), color, scale)
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    override fun widgetButtonPressed(
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
        widgetWidth: Int,
        widgetHeight: Int,
        mouseX: Int,
        mouseY: Int,
    ): Boolean {
        if (mouseX in x..x + widgetWidth && mouseY in y..y + widgetHeight) {
            Minecraft.getInstance().soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0f))
            ServerButtonPress(arrayListOf<Int>().apply {
                CURRENT_CLIENT_GUI?.getPathToNode(
                    this@ButtonWidget,
                    this
                )
            }).send()
            return true
        }
        return false
    }
}

@Serializable
@HollowPacketV2
class ServerButtonPress(val path: List<Int>) : HollowPacketV3<ServerButtonPress> {
    override fun handle(player: Player, data: ServerButtonPress) {
        (CURRENT_SERVER_GUI?.getNodeAtPath(path) as? ButtonWidget)?.onClick?.invoke()
    }

}

fun Widget.button(text: String, image: String, config: ButtonWidget.() -> Unit = {}) {
    this += ButtonWidget(text, image.rl).apply(config)
}