package ru.hollowhorizon.hc.client.screens.widget;

import net.minecraft.client.gui.components.AbstractWidget;

public interface IWidgetConsumer<T extends AbstractWidget> {
    T create(int x, int y, int width, int height);
}
