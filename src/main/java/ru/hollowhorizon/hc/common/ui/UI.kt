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
        val pos = 10.px + 20.px - 5.px
    }
//    val ui = gui {
//        layout(width=200, height=50) {
//            spread = HORIZONTAL
//            layout(width=100, height=50) {
//                button(text="Одиночная игра") {
//
//                }
//                button(text="Сетевая игра") {
//
//                }
//                button(text="Настройки") {
//
//                }
//                button(text="Выход") {
//
//                }
//            }
//            layout(width=100, height=50) {
//                layout(width=50, height=25) {
//                    image("hollow_core_logo.png".rl)
//                }
//                layout(width=50, height=25) {
//                    whenOnClient {
//                        openglContext { stack, x, y, width, height ->
//                            stack.pushPose()
//                            stack.translate(x, y, 0.0)
//                            stack.scale(width, height, 1.0)
//                            stack.translate(-width / 2, -height / 2, 0.0)
//
//                            mc.entityRenderer.renderStatic(stack, entity, 0.0, 0.0, 0.0, 0.0, 0.0, false)
//
//                            stack.popPose()
//                        }
//                    }
//                }
//            }
//        }
//    }
}