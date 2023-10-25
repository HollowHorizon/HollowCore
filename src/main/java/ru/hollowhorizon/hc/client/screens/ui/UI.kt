package ru.hollowhorizon.hc.client.screens.ui

import ru.hollowhorizon.hc.client.utils.rl

fun gui(builder: Gui.() -> Unit): () -> Gui = { Gui().apply(builder) }

class Gui {

}

fun main() {
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