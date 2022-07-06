package ru.hollowhorizon.hc.client.screens.widget;

import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.ITextComponent;

public interface INameableWidgetConsumer {
    Widget create(int x, int y, int width, int height, ITextComponent textComponent);
}