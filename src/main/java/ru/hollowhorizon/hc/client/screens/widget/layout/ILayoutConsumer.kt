package ru.hollowhorizon.hc.client.screens.widget.layout

import net.minecraft.client.gui.widget.Widget

interface ILayoutConsumer {
    fun addLayoutWidget(widget: Widget)

    fun x(): Int
    fun y(): Int

    fun width(): Int
    fun height(): Int
}