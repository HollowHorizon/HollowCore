package ru.hollowhorizon.hc.client.screens.widget.layout

import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.screens.widget.button.BaseButton
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.ui.Alignment

fun example() {
    Minecraft.getInstance().setScreen(createGui {
        padding = 10.pc x 10.pc
        placementType = PlacementType.GRID
        alignElements = Alignment.BOTTOM_CENTER

        elements {
            for(i in 0..4) {
                +BaseButton(0, 0, 70, 50, "$i".mcText, { HollowCore.LOGGER.info("$i") }, "123".rl)
                if(i % 3 == 0) lineBreak()
            }
        }
    })
}