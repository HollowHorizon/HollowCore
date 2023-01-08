package ru.hollowhorizon.hc.client.utils

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.Widget
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
import ru.hollowhorizon.hc.client.screens.widget.OriginWidget

infix fun Widget.parent(parent: HollowWidget) {
    parent.addWidget(this)



    this.x = parent.x + this.x
    this.y = parent.y + this.y
}

infix fun Widget.parent(parent: Screen) {
    parent.children.add(this)
    parent.buttons.add(this)
}