package ru.hollowhorizon.hc.client.screens.widget;

import net.minecraft.client.gui.widget.Widget;

public interface IWidgetConsumer<T extends Widget> {
    T create(int x, int y, int width, int height);
}
