package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.HollowCore.MODID
import ru.hollowhorizon.hc.client.screens.widget.layout.BoxWidget
import ru.hollowhorizon.hc.client.screens.widget.layout.box
import ru.hollowhorizon.hc.common.ui.Alignment


class ComboWidget<T : AbstractWidget>(
    text: Component,
    val elements: List<T>,
    x: Int,
    y: Int,
    w: Int,
    h: Int,
) : HollowWidget(x, y, w, h, text) {
    private var dragging = false
    private var elementBoxVisible = false
    private lateinit var elementsBox: BoxWidget

    override fun init() {
        super.init()
        box {
            size = 100.pc x 100.pc
            renderer = { stack, x, y, z, w ->
                val texture = if (isHovered) TEXT_FIELD_LIGHT else TEXT_FIELD
                drawTextInBox(stack, texture, message, x, y, width, height)
            }
        }
        elementsBox = box {
            align = Alignment.TOP_CENTER
            alignElements = Alignment.TOP_CENTER
            pos = 0.pc x 100.pc
            size = 100.pc x 500.pc
            spacing = 0.pc x 0.pc

            elements {
                this@ComboWidget.elements.forEach { +it }
            }
        }
    }

    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        stack.pushPose()
        if(elementBoxVisible) stack.translate(0.0, 0.0, 100.0)
        super.renderButton(stack, mouseX, mouseY, ticks)

        elementsBox.visible = elementBoxVisible

        if(isHovered && !elementBoxVisible) elementBoxVisible = true
        if(elementBoxVisible && !dragging && (mouseX !in x..x+width || mouseY !in y..elementsBox.y+elementsBox.height)) elementBoxVisible = false
        stack.pushPose()
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        dragging = true
        return super.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        dragging = false
        return super.mouseReleased(mouseX, mouseY, button)
    }
}

val TEXT_FIELD = ResourceLocation(MODID, "textures/gui/text_field.png")
val TEXT_FIELD_LIGHT = ResourceLocation(MODID, "textures/gui/text_field_light.png")

fun drawTextInBox(
    stack: PoseStack,
    texture: ResourceLocation,
    text: Component,
    x: Int,
    y: Int,
    width: Int,
    height: Int,
) {
    val font = Minecraft.getInstance().font


    RenderSystem.enableBlend()
    RenderSystem.defaultBlendFunc()

    RenderSystem.setShaderTexture(0, texture)
    Gui.blit(stack, x, y, 0f, 0f, 20, height, 20, height * 3)
    Gui.blit(stack, x + 20, y, 0f, 20f, width - 40, height, 20, height * 3)
    Gui.blit(stack, x + width - 20, y, 0f, 40f, 20, height, 20, height * 3)

    font.drawShadow(stack, text, x + width / 2f - font.width(text) / 2f, y + height / 2 - 4.5f, 0xFFFFFF)
}