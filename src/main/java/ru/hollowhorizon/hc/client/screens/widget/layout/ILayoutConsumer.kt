package ru.hollowhorizon.hc.client.screens.widget.layout

import net.minecraft.client.gui.components.AbstractWidget

interface ILayoutConsumer {
    fun addLayoutWidget(widget: AbstractWidget)

    fun x(): Int
    fun y(): Int

    fun width(): Int
    fun height(): Int
}