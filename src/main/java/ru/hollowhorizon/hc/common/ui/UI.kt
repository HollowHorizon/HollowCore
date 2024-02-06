package ru.hollowhorizon.hc.common.ui

import kotlinx.serialization.Serializable
import me.lucko.spark.lib.adventure.text.BlockNBTComponent.Pos
import net.minecraft.nbt.Tag
import net.minecraftforge.common.util.INBTSerializable

fun gui(builder: Gui.() -> Unit) = Gui().apply(builder)

@Serializable
class Position(var value: Float = 0f) {
    val type = Type.PIXELS
    var other = ArrayList<Position>()

    enum class Type { PIXELS, PERCENT }

    operator fun plus(other: Position): Position {
        this.other += other
        return this
    }

    operator fun minus(other: Position): Position {
        other.value *= -1
        this.other += other
        return this
    }
}

open class Widget: INBTSerializable<Tag> {


    override fun serializeNBT(): Tag {
        TODO("Not yet implemented")
    }

    override fun deserializeNBT(p0: Tag?) {
        TODO("Not yet implemented")
    }

}

class Gui: Widget() {
    val widgets = ArrayList<Widget>()

    fun size(x: Position, y: Position) {}
    fun padding(top: Position = 0.px, bottom: Position = 0.px, left: Position = 0.px, right: Position = 0.px) {}

    fun align(center: Alignment) {}

    val Int.px get() = Position(this.toFloat())
}

fun main() {
    gui {
        size(90.px, 90.px)
        padding(left = 5.px)
        align(Alignment.CENTER)
    }
}

