package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.*
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.narration.NarrationElementOutput
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.ItemStack
import net.minecraftforge.client.ForgeHooksClient
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.screens.widget.layout.ILayoutConsumer
import ru.hollowhorizon.hc.common.ui.Alignment

open class HollowWidget(x: Int, y: Int, width: Int, height: Int, text: Component) :
    AbstractWidget(x, y, width, height, text), ILayoutConsumer {
    @JvmField
    val widgets = ArrayList<AbstractWidget>()
    protected val textureManager: TextureManager = Minecraft.getInstance().textureManager
    protected val font = Minecraft.getInstance().font
    private var isInitialized = false

    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        if (!isInitialized) {
            init()
            isInitialized = true
        }

        widgets.forEach { widget ->
            if (!widget.visible) return@forEach

            renderWidget(widget, stack, mouseX, mouseY, ticks)
        }
    }

    open fun renderWidget(widget: AbstractWidget, stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        widget.render(stack, mouseX, mouseY, ticks)
    }

    open fun init() {}
    fun <T : AbstractWidget> addWidget(widget: T): T {
        widgets.add(widget)
        return widget
    }

    fun addWidgets(vararg widgets: AbstractWidget) {
        this.widgets.addAll(listOf(*widgets))
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!visible) return false

        var isClicked = false
        for (widget in widgets) {
            if (widget.visible) isClicked = widgetMouseClicked(widget, mouseX, mouseY, button) || isClicked
        }

        return super.mouseClicked(mouseX, mouseY, button) || isClicked
    }

    open fun widgetMouseClicked(widget: AbstractWidget, mouseX: Double, mouseY: Double, button: Int): Boolean {
        return widget.mouseClicked(mouseX, mouseY, button)
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!visible) return false

        var isReleased = false
        for (widget in widgets) {
            if (widget.visible) isReleased = widgetMouseReleased(widget, mouseX, mouseY, button) || isReleased
        }

        return super.mouseReleased(mouseX, mouseY, button) || isReleased
    }

    open fun widgetMouseReleased(widget: AbstractWidget, mouseX: Double, mouseY: Double, button: Int): Boolean {
        return widget.mouseReleased(mouseX, mouseY, button)
    }

    override fun mouseDragged(mouseX: Double, mouseY: Double, button: Int, dragX: Double, dragY: Double): Boolean {
        if (!visible) return false

        var isDragged = false
        for (widget in widgets) {
            if (widget.visible) isDragged =
                widgetMouseDragged(widget, mouseX, mouseY, button, dragX, dragY) || isDragged
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY) || isDragged
    }

    open fun widgetMouseDragged(
        widget: AbstractWidget,
        mouseX: Double,
        mouseY: Double,
        button: Int,
        dragX: Double,
        dragY: Double,
    ): Boolean {
        return widget.mouseDragged(mouseX, mouseY, button, dragX, dragY)
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        if (!visible) return false

        var isScrolled = false
        for (widget in widgets) {
            if (widget.visible) isScrolled = widgetMouseScrolled(widget, mouseX, mouseY, scroll) || isScrolled
        }

        return super.mouseScrolled(mouseX, mouseY, scroll) || isScrolled
    }

    open fun widgetMouseScrolled(widget: AbstractWidget, mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        return widget.mouseScrolled(mouseX, mouseY, scroll)
    }

    override fun mouseMoved(mouseX: Double, mouseY: Double) {
        if (!visible) return

        for (widget in widgets) {
            if (widget.visible) widgetMouseMoved(widget, mouseX, mouseY)
        }

        super.mouseMoved(mouseX, mouseY)
    }

    open fun widgetMouseMoved(widget: AbstractWidget, mouseX: Double, mouseY: Double) {
        widget.mouseMoved(mouseX, mouseY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible) return false

        var isPressed = false
        for (widget in widgets) {
            if (widget.visible) isPressed = widget.keyPressed(keyCode, scanCode, modifiers) || isPressed
        }

        return super.keyPressed(keyCode, scanCode, modifiers) || isPressed
    }

    override fun keyReleased(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        if (!visible) return false

        var isReleased = false
        for (widget in widgets) {
            if (widget.visible) isReleased = widget.keyReleased(keyCode, scanCode, modifiers) || isReleased
        }

        return super.keyReleased(keyCode, scanCode, modifiers) || isReleased
    }

    override fun charTyped(character: Char, p_231042_2_: Int): Boolean {
        if (!visible) return false

        var isTyped = false
        for (widget in widgets) {
            if (widget.visible) isTyped = widget.charTyped(character, p_231042_2_) || isTyped
        }

        return super.charTyped(character, p_231042_2_) || isTyped
    }

    override fun updateNarration(pNarrationElementOutput: NarrationElementOutput) {

    }

    fun bind(modid: String, path: String) {
        bind(ResourceLocation(modid, "textures/$path"))

    }

    fun bind(path: ResourceLocation) {
        RenderSystem.setShaderTexture(0, path)
    }


    override fun playDownSound(pHandler: SoundManager) {}

    @JvmOverloads
    fun betterBlit(
        stack: PoseStack,
        alignment: Alignment,
        offsetX: Int,
        offsetY: Int,
        targetWidth: Int,
        targetHeight: Int,
        imageWidth: Int = targetWidth,
        imageHeight: Int = targetHeight,
        texX: Int = 0,
        texY: Int = 0,
        size: Float = 1.0f,
    ) {
        blit(
            stack,
            HollowScreen.getAlignmentPosX(alignment, offsetX + this.x, width, targetWidth, size),
            HollowScreen.getAlignmentPosY(alignment, offsetY - this.y, height, targetHeight, size),
            texX.toFloat(),
            texY.toFloat(),
            (targetWidth * size).toInt(),
            (targetHeight * size).toInt(),
            (imageWidth * size).toInt(),
            (imageHeight * size).toInt()
        )
    }

    fun setX(x: Int) {
        val lx = this.x
        this.x = x
        for (widget in widgets) {
            if (widget is HollowWidget) {
                widget.setX(widget.x - lx + x)
            } else {
                widget.x = widget.x - lx + x
            }
        }
    }

    fun setY(y: Int) {
        val ly = this.y
        this.y = y
        for (widget in widgets) {
            if (widget is HollowWidget) {
                widget.setY(widget.y - ly + y)
            } else {
                widget.y = widget.y - ly + y
            }
        }
    }

    override fun addLayoutWidget(widget: AbstractWidget) {
        this.addWidget(widget)
    }

    open fun renderTooltipInternal(
        pPoseStack: PoseStack,
        pClientTooltipComponents: List<ClientTooltipComponent>,
        pMouseX: Int,
        pMouseY: Int,
    ) {
        if (!pClientTooltipComponents.isEmpty()) {
            val preEvent = ForgeHooksClient.onRenderTooltipPre(
                ItemStack.EMPTY,
                pPoseStack,
                pMouseX,
                pMouseY,
                width,
                height,
                pClientTooltipComponents,
                this.font,
                font
            )
            if (preEvent.isCanceled) return
            var i = 0
            var j = if (pClientTooltipComponents.size == 1) -2 else 0
            for (clienttooltipcomponent in pClientTooltipComponents) {
                val k = clienttooltipcomponent.getWidth(preEvent.font)
                if (k > i) {
                    i = k
                }
                j += clienttooltipcomponent.height
            }
            var j2 = preEvent.x + 12
            var k2 = preEvent.y - 12
            if (j2 + i > width) {
                j2 -= 28 + i
            }
            if (k2 + j + 6 > height) {
                k2 = height - j - 6
            }
            pPoseStack.pushPose()
            val f: Float = Minecraft.getInstance().itemRenderer.blitOffset
            Minecraft.getInstance().itemRenderer.blitOffset = 400.0f
            val tesselator = Tesselator.getInstance()
            val bufferbuilder = tesselator.builder
            RenderSystem.setShader { GameRenderer.getPositionColorShader() }
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
            val matrix4f = pPoseStack.last().pose()
            val colorEvent = ForgeHooksClient.onRenderTooltipColor(
                ItemStack.EMPTY,
                pPoseStack,
                j2,
                k2,
                preEvent.font,
                pClientTooltipComponents
            )
            fillGradient(
                matrix4f,
                bufferbuilder,
                j2 - 3,
                k2 - 4,
                j2 + i + 3,
                k2 - 3,
                400,
                colorEvent.backgroundStart,
                colorEvent.backgroundStart
            )
            fillGradient(
                matrix4f,
                bufferbuilder,
                j2 - 3,
                k2 + j + 3,
                j2 + i + 3,
                k2 + j + 4,
                400,
                colorEvent.backgroundEnd,
                colorEvent.backgroundEnd
            )
            fillGradient(
                matrix4f,
                bufferbuilder,
                j2 - 3,
                k2 - 3,
                j2 + i + 3,
                k2 + j + 3,
                400,
                colorEvent.backgroundStart,
                colorEvent.backgroundEnd
            )
            fillGradient(
                matrix4f,
                bufferbuilder,
                j2 - 4,
                k2 - 3,
                j2 - 3,
                k2 + j + 3,
                400,
                colorEvent.backgroundStart,
                colorEvent.backgroundEnd
            )
            fillGradient(
                matrix4f,
                bufferbuilder,
                j2 + i + 3,
                k2 - 3,
                j2 + i + 4,
                k2 + j + 3,
                400,
                colorEvent.backgroundStart,
                colorEvent.backgroundEnd
            )
            fillGradient(
                matrix4f,
                bufferbuilder,
                j2 - 3,
                k2 - 3 + 1,
                j2 - 3 + 1,
                k2 + j + 3 - 1,
                400,
                colorEvent.borderStart,
                colorEvent.borderEnd
            )
            fillGradient(
                matrix4f,
                bufferbuilder,
                j2 + i + 2,
                k2 - 3 + 1,
                j2 + i + 3,
                k2 + j + 3 - 1,
                400,
                colorEvent.borderStart,
                colorEvent.borderEnd
            )
            fillGradient(
                matrix4f,
                bufferbuilder,
                j2 - 3,
                k2 - 3,
                j2 + i + 3,
                k2 - 3 + 1,
                400,
                colorEvent.borderStart,
                colorEvent.borderStart
            )
            fillGradient(
                matrix4f,
                bufferbuilder,
                j2 - 3,
                k2 + j + 2,
                j2 + i + 3,
                k2 + j + 3,
                400,
                colorEvent.borderEnd,
                colorEvent.borderEnd
            )
            RenderSystem.enableDepthTest()
            RenderSystem.disableTexture()
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
            BufferUploader.drawWithShader(bufferbuilder.end())
            RenderSystem.disableBlend()
            RenderSystem.enableTexture()
            val source = MultiBufferSource.immediate(Tesselator.getInstance().builder)
            pPoseStack.translate(0.0, 0.0, 400.0)
            var l1 = k2
            for (i2 in pClientTooltipComponents.indices) {
                val component = pClientTooltipComponents[i2]
                component.renderText(preEvent.font, j2, l1, matrix4f, source)
                l1 += component.height + if (i2 == 0) 2 else 0
            }
            source.endBatch()
            pPoseStack.popPose()
            l1 = k2
            for (l2 in pClientTooltipComponents.indices) {
                val component = pClientTooltipComponents[l2]
                component.renderImage(preEvent.font, j2, l1, pPoseStack, Minecraft.getInstance().itemRenderer, 400)
                l1 += component.height + if (l2 == 0) 2 else 0
            }
            Minecraft.getInstance().itemRenderer.blitOffset = f
        }
    }

    override fun x() = this.x
    override fun y() = this.y
    override fun width() = this.width
    override fun height() = this.height

    fun isHovered(mouseX: Double, mouseY: Double): Boolean {
        return mouseX >= this.x && mouseX < this.x + this.width && mouseY >= this.y && mouseY < this.y + this.height
    }

    open fun tick() {
        this.widgets.forEach { if (it is HollowWidget) it.tick() }
    }
}