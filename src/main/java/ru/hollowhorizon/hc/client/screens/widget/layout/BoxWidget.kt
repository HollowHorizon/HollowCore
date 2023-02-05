package ru.hollowhorizon.hc.client.screens.widget.layout

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.widget.Widget
import net.minecraft.util.math.MathHelper
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.screens.DrawUtils
import ru.hollowhorizon.hc.client.screens.util.Alignment
import ru.hollowhorizon.hc.client.screens.util.IPlacement
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
import ru.hollowhorizon.hc.client.screens.widget.OriginWidget
import ru.hollowhorizon.hc.client.screens.widget.VerticalSliderWidget
import ru.hollowhorizon.hc.client.utils.toSTC

class BoxWidget(
    x: Int,
    y: Int,
    width: Int,
    height: Int,
    name: String,
    val renderer: (MatrixStack, Int, Int, Int, Int) -> Unit,
    val padding: SizePair,
) : OriginWidget(x, y, width, height) {
    override fun playDownSound(p_230988_1_: SoundHandler) {}

    private var currentHeight = 0
    var verticalSlider: VerticalSliderWidget? = null

    override fun init() {
        super.init()

        verticalSlider = this.addWidget(
            VerticalSliderWidget((this.x + this.width - 20), this.y, 20, this.height)
        )
        verticalSlider?.onValueChange { value ->
            originY = (maxHeight * value).toInt()
        }
    }

    override fun renderButton(stack: MatrixStack, mouseX: Int, mouseY: Int, ticks: Float) {
        checkSliders()

        renderer(stack, x, y, width, height)

        super.renderButton(stack, mouseX, mouseY, ticks)

        if (HollowCore.DEBUG_MODE) {
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
        val outOfBoundsY = this.widgets.any { it.y + it.height > this.y + this.height || it.y < this.y }

        verticalSlider?.visible = outOfBoundsY
        verticalSlider?.active = outOfBoundsY

        canMove = outOfBoundsY
        canScale = outOfBoundsY
    }

    override fun mouseScrolled(mouseX: Double, mouseY: Double, scroll: Double): Boolean {
        if (maxHeight > 0) {
            currentHeight -= (scroll * 5).toInt()
            currentHeight = MathHelper.clamp(currentHeight, 0, maxHeight)

            this.verticalSlider?.scroll = currentHeight / (maxHeight + 0.0f)

        }
        return super.mouseScrolled(mouseX, mouseY, scroll)
    }

    fun hide() {
        visible = false
    }

    fun show() {
        visible = true
    }
}

class BoxBuilder(val x0: Int, val y0: Int, val maxWidth: Int, val maxHeight: Int) {
    val widgets: MutableList<Widget> = ArrayList()
    var align: IPlacement = Alignment.CENTER
    var size: SizePair = 90.pc x 90.pc

    // Pos initializing using [left x right] structure
    var pos: SizePair = 0.px x 0.px
    var padding: SizePair = 0.px x 0.px

    var renderer: (MatrixStack, Int, Int, Int, Int) -> Unit = { stack, x, y, w, h -> }

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

    fun add(widget: Widget) {
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
    val widgets: MutableList<Widget> = ArrayList()

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

    operator fun <T : Widget> T.unaryPlus(): T {
        widgets.add(this)
        val align = prev.alignElements

        when (prev.placementType) {
            PlacementType.GRID -> {
                val sizedWidgets = arrayListOf<List<Widget>>()

                var currentRow = arrayListOf<Widget>()
                var currentRowWidth = 0
                var maxHeight = 0

                widgets.forEach { widget ->
                    if (currentRowWidth + widget.width > width()) {
                        sizedWidgets.add(currentRow)
                        maxHeight += (currentRow.maxOfOrNull { it.height } ?: 0) + prev.spacing.height.value
                        currentRow = arrayListOf()
                        currentRowWidth = 0
                    }

                    currentRow.add(widget)
                    currentRowWidth += widget.width + prev.spacing.width.value
                }

                if (currentRow.isNotEmpty()) {
                    sizedWidgets.add(currentRow)
                    maxHeight += currentRow.maxOf { it.height } + prev.spacing.height.value
                }

                val freeHeight = height() - maxHeight + prev.spacing.height.value + 1 // +1 to fix when factorY is 1.0

                var yDelta = (y() + freeHeight * align.factorY()).toInt()

                sizedWidgets.forEach { rowWidgets ->
                    val freeWidth =
                        width() - rowWidgets.sumOf { it.width + prev.spacing.width.value } + prev.spacing.width.value

                    var xDelta = (x() + freeWidth * align.factorX()).toInt()

                    rowWidgets.forEach { widget ->
                        widget.x = xDelta
                        widget.y = yDelta

                        xDelta += widget.width + prev.spacing.width.value
                    }

                    yDelta += (rowWidgets.maxOfOrNull { it.height } ?: 0) + prev.spacing.height.value
                }
            }

            PlacementType.VERTICAL -> {
                val freeWidth =
                    width() - widgets.maxOf { it.width } - prev.spacing.width.value
                val freeHeight =
                    height() - widgets.sumOf { it.height + prev.spacing.height.value } + prev.spacing.height.value

                var yDelta = (y() + freeHeight * align.factorY()).toInt()

                widgets.forEach { widget ->
                    widget.x = (x() + freeWidth * align.factorX() - widget.width * align.factorX()).toInt()
                    widget.y = yDelta

                    yDelta += widget.height + prev.spacing.height.value
                }
            }

            PlacementType.HORIZONTAL -> {
                val freeWidth =
                    width() - widgets.sumOf { it.width + prev.spacing.width.value } + prev.spacing.width.value
                val freeHeight =
                    height() - widgets.maxOf { it.height } - prev.spacing.height.value

                var xDelta = (x() + freeWidth * align.factorX()).toInt()

                widgets.forEach { widget ->
                    widget.x = xDelta
                    widget.y =
                        (y() + (height() - freeHeight) * align.factorY() - widget.height * align.factorY()).toInt()

                    xDelta += widget.width + prev.spacing.width.value
                }
            }
        }

        return this
    }

    override fun addLayoutWidget(widget: Widget) {
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

                else -> raw
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

fun ILayoutConsumer.box(name: String = "", builder: BoxBuilder.() -> Unit): BoxWidget {
    val boxBuilder = BoxBuilder(this.x(), this.y(), this.width(), this.height())
    boxBuilder.builder()
    val box = BoxWidget(
        boxBuilder.x(),
        boxBuilder.y(),
        boxBuilder.width(),
        boxBuilder.height(),
        name,
        boxBuilder.renderer,
        boxBuilder.padding
    )
    for (widget in boxBuilder.widgets) {
        box.addLayoutWidget(widget)
    }
    this.addLayoutWidget(box)
    return box
}