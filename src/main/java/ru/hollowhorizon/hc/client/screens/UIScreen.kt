package ru.hollowhorizon.hc.client.screens

import ru.hollowhorizon.hc.client.screens.widget.ComboWidget
import ru.hollowhorizon.hc.client.screens.widget.SwitchWidget
import ru.hollowhorizon.hc.client.screens.widget.layout.box
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.toSTC


class UIScreen : HollowScreen("".toSTC()) {
    override fun init() {
        super.init()

        box {
            size = 90.pc x 90.pc

            renderer = { stack, x, y, w, h ->
                fillGradient(stack, x, y, x + w, y + h, 0x4287F5FF, 0x224C8FFF)
            }

            elements {
                +ComboWidget(
                    "вариантЫ:".mcText,
                    Array(10) { SwitchWidget(0, 0, 20.pc.w().value, 20) { } }.toList(),
                    0, 0, 25.pc.w().value, 20
                )
            }
        }
    }
}