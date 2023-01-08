package ru.hollowhorizon.hc.client.screens.widget

import net.minecraft.client.audio.SoundHandler
import net.minecraft.client.gui.widget.Widget
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.screens.util.Alignment
import ru.hollowhorizon.hc.client.utils.parent
import ru.hollowhorizon.hc.client.utils.toSTC

class BoxWidget(x: Int, y: Int, width: Int, height: Int) : HollowWidget(x, y, width, height, "".toSTC()) {
    override fun playDownSound(p_230988_1_: SoundHandler) {}

    fun hide() {
        visible = false
    }

    fun show() {
        visible = true
    }
}

class BoxBuilder(val startX: Int, val startY: Int, val width: Int, val height: Int) {
    val widgets: MutableList<Widget> = ArrayList()
    var alignment: Alignment = Alignment.CENTER
    var size: IntPair = 100f x 100f
    var offset: IntPair = 0f x 0f

    fun widgets(value: (Int, Int, Int, Int) -> Unit) {
        value(x(), y(), size.width, size.height)
    }

    fun add(widget: Widget) {
        widgets.add(widget)
    }

    infix fun Float.x(fl: Float): IntPair {
        return IntPair((this * width / 100f).toInt(), (fl * height / 100f).toInt())
    }

    infix fun Int.x(fl: Int): IntPair {
        return IntPair(this, fl)
    }

    infix fun Int.x(fl: Float): IntPair {
        return IntPair(this, (fl * height / 100f).toInt())
    }

    infix fun Float.x(fl: Int): IntPair {
        return IntPair((this * width / 100f).toInt(), fl)
    }

    fun Float.h(): Int {
        return (this * height / 100f).toInt()
    }

    fun Float.w(): Int {
        return (this * width / 100f).toInt()
    }

    fun x(): Int = (startX + offset.width + alignment.factorX() * width - alignment.factorX() * size.width).toInt()
    fun y(): Int = (startY + offset.height + alignment.factorY() * height - alignment.factorY() * size.height).toInt()
}

data class IntPair(val width: Int, val height: Int)

fun HollowWidget.box(builder: BoxBuilder.() -> Unit): BoxWidget {
    val boxBuilder = BoxBuilder(this.x, this.y, this.width, this.height)
    boxBuilder.builder()
    val box = BoxWidget(boxBuilder.x(), boxBuilder.y(), boxBuilder.size.width, boxBuilder.size.height)
    box parent this
    for (widget in boxBuilder.widgets) {
        box.addWidget(widget)
    }
    return box
}

fun HollowScreen.box(builder: BoxBuilder.() -> Unit): BoxWidget {
    val boxBuilder = BoxBuilder(0, 0, this.width, this.height)
    boxBuilder.builder()
    val box = BoxWidget(boxBuilder.x(), boxBuilder.y(), boxBuilder.size.width, boxBuilder.size.height)
    box parent this
    for (widget in boxBuilder.widgets) {
        box.addWidget(widget)
    }
    return box
}

fun BoxBuilder.box(builder: BoxBuilder.() -> Unit): BoxWidget {
    val boxBuilder = BoxBuilder(this.x(), this.y(), this.size.width, this.size.height)
    boxBuilder.builder()
    val box = BoxWidget(boxBuilder.x(), boxBuilder.y(), boxBuilder.size.width, boxBuilder.size.height)
    for (widget in boxBuilder.widgets) {
        box.addWidget(widget)
    }
    this.widgets.add(box)
    return box
}