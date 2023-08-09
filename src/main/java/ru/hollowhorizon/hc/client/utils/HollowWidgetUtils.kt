package ru.hollowhorizon.hc.client.utils

import net.minecraft.client.gui.components.AbstractWidget
import net.minecraft.client.gui.screens.Screen
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget
import ru.hollowhorizon.hc.mixin.ScreenAccessor

infix fun AbstractWidget.parent(parent: HollowWidget) {
    parent.addLayoutWidget(this)



    this.x = parent.x + this.x
    this.y = parent.y + this.y
}

infix fun AbstractWidget.parent(parent: Screen) {
    (parent as ScreenAccessor).apply {
        children().add(this@parent)
        renderables().add(this@parent)
    }
}