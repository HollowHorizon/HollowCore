package ru.hollowhorizon.hc.client.screens.widget;


import net.minecraft.network.chat.Component;

import static ru.hollowhorizon.hc.client.utils.ForgeKotlinKt.toSTC;

public class WidgetBox extends HollowWidget {
    private final int sizeX;
    private final int sizeY;
    private final int boarderX;
    private final int boarderY;
    private final int boxSizeX;
    private final int boxSizeY;

    public WidgetBox(int x, int y, int width, int height, int sizeX, int sizeY, int boarderX, int boarderY) {
        super(x, y, width, height, toSTC("WIDGET_BOX"));
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.boarderX = boarderX;
        this.boarderY = boarderY;

        this.boxSizeX = (this.width - (sizeX - 1) * boarderX) / sizeX;
        this.boxSizeY = (this.height - (sizeY - 1) * boarderY) / sizeY;

        init();
    }

    @Override
    public void init() {
        this.widgets.clear();
    }

    public void putWidget(int xBox, int yBox, IWidgetConsumer<?> widget) {
        if (xBox < 1 || xBox > sizeX) return;
        if (yBox < 1 || yBox > sizeY) return;

        int widgetX = this.x + boxSizeX * (xBox - 1) + boarderX * (xBox - 1);
        int widgetY = this.x + boxSizeY * (yBox - 1) + boarderY * (yBox - 1);

        this.widgets.add(widget.create(widgetX, widgetY, boxSizeX, boxSizeY));
    }
}
