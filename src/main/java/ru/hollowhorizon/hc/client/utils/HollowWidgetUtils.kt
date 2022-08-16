package ru.hollowhorizon.hc.client.utils

import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.Widget
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget

infix fun Widget.parent(parent: HollowWidget) {
    parent.addWidget(this)
}

infix fun Widget.parent(parent: Screen) {
    parent.children.add(this)
    parent.buttons.add(this)
}