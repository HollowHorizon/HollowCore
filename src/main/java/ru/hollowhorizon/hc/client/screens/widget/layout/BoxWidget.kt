package ru.hollowhorizon.hc.client.screens.widget.layout

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.sounds.SoundManager
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.screens.DrawUtils
import ru.hollowhorizon.hc.client.screens.util.Alignment
import ru.hollowhorizon.hc.client.screens.util.IPlacement
import ru.hollowhorizon.hc.client.screens.widget.HorizontalSliderWidget
import ru.hollowhorizon.hc.client.screens.widget.OriginWidget
import ru.hollowhorizon.hc.client.screens.widget.VerticalSliderWidget

class BoxWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    val renderer: (PoseStack, Int, Int, Int, Int) -> Unit,
    val padding: SizePair,
) : OriginWidget(x, y, width, height) {
    override var canScale = false


    override fun playDownSound(p_230988_1_: SoundManager) {}

    var verticalSlider: VerticalSliderWidget? = null
    var horizontalSlider: HorizontalSliderWidget? = null
    var offsetX = 0
    var offsetY = 0

    override fun init() {
        super.init()

        this.originY = offsetY
        this.originX = offsetX

        horizontalSlider = this.addWidget(
            HorizontalSliderWidget(this.x, this.y + this.height - 10, this.width - 10, 10)
        )
        horizontalSlider?.onValueChange { value ->
            originX = offsetX + (maxWidth * value).toInt()
        }

        verticalSlider = this.addWidget(
            VerticalSliderWidget((this.x + this.width - 10), this.y, 10, this.height - 10)
        )
        verticalSlider?.onValueChange { value ->
            originY = offsetY + (maxHeight * value).toInt()
        }

        canMove = false
        checkSliders()
    }

    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        renderer(stack, x, y, width, height)

        super.renderButton(stack, mouseX, mouseY, ticks)

        if (!HollowCore.DEBUG_MODE) {
            //box
            DrawUtils.drawBounds(
                stack, this.x, this.y, this.x + this.width, this.y + this.height, 1, 0xFFFFFFFF.toInt()
            )

            //padding
            DrawUtils.drawBounds(
                stack,
                this.x + padding.halfWidth(),
                this.y + padding.halfHeight(),
                this.x + this.width - padding.halfWidth(),
                this.y + this.height - padding.halfHeight(),
                1,
                0x34EB7AFF
            )
        }
    }

    private fun checkSliders() {
        val widgets = this.widgets.filter { it !is LineBreak }

        val outOfBoundsY = widgets.any { it.y + it.height > this.y + this.height || it.y < this.y }
        val outOfBoundsX = widgets.any { it.x + it.width > this.x + this.width || it.x < this.x }

        verticalSlider?.visible = outOfBoundsY
        verticalSlider?.active = outOfBoundsY

        horizontalSlider?.visible = outOfBoundsX
        horizontalSlider?.active = outOfBoundsX

        //canMove = outOfBoundsX || outOfBoundsY

        if (maxWidth <= 0 && maxHeight > 0) this.verticalSlider?.height = this.height
        if (maxHeight <= 0 && maxWidth > 0) this.horizontalSlider?.width = this.width
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        if (isHovered(mouseX, mouseY)) {
            val scrollVal = -scroll.toFloat() / 100f
            if (maxHeight > 0) {
                if (this.verticalSlider != null) {
                    this.verticalSlider!!.scroll += scrollVal
                }
            }
            if (maxWidth > 0) {
                val flag = if(maxHeight == 0) true else (Screen.hasShiftDown() || Screen.hasControlDown())
                if (this.horizontalSlider != null && flag) {
                    this.horizontalSlider!!.scroll += scrollVal
                }
            }
        }
        return super.mouseScrolled(mouseX, mouseY, scroll)
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (!isHovered) return false
        return super.mouseClicked(mouseX, mouseY, button)
    }

    fun hide() {
        visible = false
    }

    fun show() {
        visible = true
    }
}

class BoxBuilder(val x0: Int, val y0: Int, val maxWidth: Int, val maxHeight: Int) {
    val widgets: MutableList<AbstractWidget> = ArrayList()
    var align: IPlacement = Alignment.CENTER
    var size: SizePair = 90.pc x 90.pc

    // Pos initializing using [left x right] structure
    var pos: SizePair = 0.px x 0.px
    var padding: SizePair = 0.px x 0.px

    var renderer: (PoseStack, Int, Int, Int, Int) -> Unit = { _, _, _, _, _ -> }

    var alignElements: IPlacement = Alignment.CENTER
    var placementType: PlacementType = PlacementType.VERTICAL
    var spacing: SizePair = 5.pc x 5.pc

    // Int can be converted to a [Pixels (px)], [Percent Of Parent (pc)] or [Percent Of Screen (pcs)]
    val Int.px
        get() = ScreenPos(this, maxWidth, maxHeight, padding)
    val Int.pc
        get() = ScreenPos(this, maxWidth, maxHeight, padding).apply { type = ScreenPos.PosType.PARENT }
    val Int.pcs
        get() = ScreenPos(this, maxWidth, maxHeight, padding).apply { type = ScreenPos.PosType.SCREEN }

    operator fun ScreenPos.minus(another: ScreenPos): ScreenPos {
        return ScreenPos(this.value - another.value, maxWidth, maxHeight, padding)
    }

    operator fun ScreenPos.plus(another: ScreenPos): ScreenPos {
        return ScreenPos(this.value + another.value, maxWidth, maxHeight, padding)
    }

    fun elements(widgetConsumer: WidgetBuilder.() -> Unit) {
        val widgetBuilder = WidgetBuilder(this)
        widgetConsumer(widgetBuilder)
        widgets.addAll(widgetBuilder.widgets)
    }

    fun add(widget: AbstractWidget) {
        widgets.add(widget)
    }

    infix fun ScreenPos.x(pos2: ScreenPos): SizePair {
        if (!this.forceType) this.apply { isWidth = true }
        if (!pos2.forceType) pos2.apply { isWidth = false }
        return SizePair(this, pos2)
    }

    fun x(): Int =
        (x0 + pos.width.value + align.factorX() * maxWidth - align.factorX() * size.width.value).toInt()

    fun y(): Int =
        (y0 + pos.height.value + align.factorY() * maxHeight - align.factorY() * size.height.value).toInt()

    fun width(): Int {
        return size.width.value
    }

    fun height(): Int {
        return size.height.value
    }
}

class WidgetBuilder(
    val prev: BoxBuilder,
) : ILayoutConsumer {
    val font = Minecraft.getInstance().font
    val widgets: MutableList<AbstractWidget> = ArrayList()

    val Int.px
        get() = ScreenPos(this, prev.width(), prev.height(), prev.padding)
    val Int.pc
        get() = ScreenPos(this, prev.width(), prev.height(), prev.padding).apply { type = ScreenPos.PosType.PARENT }
    val Int.pcs
        get() = ScreenPos(this, prev.width(), prev.height(), prev.padding).apply { type = ScreenPos.PosType.SCREEN }

    operator fun ScreenPos.minus(another: ScreenPos): ScreenPos {
        return ScreenPos(this.value - another.value, prev.width(), prev.height(), prev.padding)
    }

    operator fun ScreenPos.plus(another: ScreenPos): ScreenPos {
        return ScreenPos(this.value + another.value, prev.width(), prev.height(), prev.padding)
    }

    fun lineBreak() = +LineBreak()

    operator fun <T : AbstractWidget> T.unaryPlus(): T {
        widgets.add(this)
        val align = prev.alignElements

        when (prev.placementType) {
            PlacementType.GRID -> {
                val sizedWidgets = arrayListOf<List<AbstractWidget>>()

                var currentRow = arrayListOf<AbstractWidget>()
                var currentRowWidth = 0
                var maxHeight = 0

                widgets.forEach { widget ->
                    if (currentRowWidth + widget.width > width() || widget is LineBreak) {
                        sizedWidgets.add(currentRow)
                        maxHeight += (currentRow.maxOfOrNull { it.height } ?: 0) + prev.spacing.height.value
                        currentRow = arrayListOf()
                        currentRowWidth = 0
                    }

                    if(widget !is LineBreak) {
                        currentRow.add(widget)
                        currentRowWidth += widget.width + prev.spacing.width.value
                    }
                }

                if (currentRow.isNotEmpty()) {
                    sizedWidgets.add(currentRow)
                    maxHeight += currentRow.maxOf { it.height } + prev.spacing.height.value
                }

                val freeHeight = height() - maxHeight + prev.spacing.height.value + 1 // +1 to fix when factorY is 1.0

                var yDelta = (y() + freeHeight * align.factorY()).toInt()

                sizedWidgets.forEach { rowWidgets ->
                    val freeWidth =
                        width() - rowWidgets.filter { it !is LineBreak }.sumOf { it.width + prev.spacing.width.value } + prev.spacing.width.value

                    var xDelta = (x() + freeWidth * align.factorX()).toInt()

                    rowWidgets.filter { it !is LineBreak }.forEach { widget ->
                        widget.x = xDelta
                        widget.y = yDelta

                        xDelta += widget.width + prev.spacing.width.value
                    }

                    yDelta += (rowWidgets.filter { it !is LineBreak }.maxOfOrNull { it.height } ?: 0) + prev.spacing.height.value
                }
            }

            PlacementType.VERTICAL -> {
                val freeHeight =
                    height() - widgets.sumOf { it.height + prev.spacing.height.value } + prev.spacing.height.value

                var yDelta = (y() + freeHeight * align.factorY()).toInt()

                widgets.forEach { widget ->

                    widget.x = (x() + width() * align.factorX() - widget.width * align.factorX()).toInt()
                    widget.y = yDelta

                    yDelta += widget.height + prev.spacing.height.value
                }
            }

            PlacementType.HORIZONTAL -> {
                val freeWidth =
                    width() - widgets.sumOf { it.width + prev.spacing.width.value } + prev.spacing.width.value

                var xDelta = (x() + freeWidth * align.factorX()).toInt()

                widgets.forEach { widget ->
                    widget.x = xDelta
                    widget.y =
                        (y() + height() * align.factorY() - widget.height * align.factorY()).toInt()

                    xDelta += widget.width + prev.spacing.width.value
                }
            }
        }

        return this
    }

    override fun addLayoutWidget(widget: AbstractWidget) {
        widgets.add(widget)
    }

    override fun x() = prev.x() + prev.padding.halfWidth()

    override fun y() = prev.y() + prev.padding.halfHeight()

    override fun width() = prev.width() - prev.padding.width.value

    override fun height() = prev.height() - prev.padding.height.value
}

enum class PlacementType {
    HORIZONTAL, VERTICAL, GRID
}

class SizePair(val width: ScreenPos, val height: ScreenPos) {
    fun halfWidth() = width.value / 2
    fun halfHeight() = height.value / 2
}

class ScreenPos(val raw: Int, private val maxWidth: Int, private val maxHeight: Int, private val padding: SizePair?) {
    var isWidth: Boolean = true
    var forceType = false
    var type = PosType.PIXELS

    val value: Int
        get() {
            return when (type) {
                PosType.PIXELS -> raw
                PosType.PARENT -> if (isWidth) ((raw / 100F) * (maxWidth - (padding?.width?.value
                    ?: 0))).toInt() else ((raw / 100F) * (maxHeight - (padding?.height?.value ?: 0))).toInt()

                PosType.SCREEN -> {
                    val window = Minecraft.getInstance().window
                    val windowX = window.guiScaledWidth
                    val windowY = window.guiScaledHeight
                    if (isWidth) ((raw / 100F) * windowX).toInt() else ((raw / 100F) * windowY).toInt()
                }

            }
        }

    fun w(): ScreenPos {
        isWidth = true
        forceType = true
        return this
    }

    fun h(): ScreenPos {
        isWidth = false
        forceType = true
        return this
    }

    enum class PosType {
        SCREEN,
        PARENT,
        PIXELS
    }
}

fun ILayoutConsumer.box(builder: BoxBuilder.() -> Unit): BoxWidget {
    val boxBuilder = BoxBuilder(this.x(), this.y(), this.width(), this.height())
    boxBuilder.builder()
    val box = BoxWidget(
        boxBuilder.x(),
        boxBuilder.y(),
        boxBuilder.width(),
        boxBuilder.height(),
        boxBuilder.renderer,
        boxBuilder.padding
    )
    for (widget in boxBuilder.widgets) {
        box.addLayoutWidget(widget)
    }
    if (box.maxHeight > 0) box.offsetY = (-box.maxHeight * boxBuilder.align.factorY()).toInt()
    if (box.maxWidth > 0) box.offsetX = (-box.maxWidth * boxBuilder.align.factorX()).toInt()
    HollowCore.LOGGER.info("Box originY: ${box.originY}")
    this.addLayoutWidget(box)
    return box
}