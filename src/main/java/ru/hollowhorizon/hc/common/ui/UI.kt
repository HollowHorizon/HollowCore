package ru.hollowhorizon.hc.common.ui

import kotlinx.serialization.Serializable

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

@Serializable
class Gui {
    val Int.px get() = Position(this.toFloat())
}

fun main() {
    val gui = gui {

    }
}