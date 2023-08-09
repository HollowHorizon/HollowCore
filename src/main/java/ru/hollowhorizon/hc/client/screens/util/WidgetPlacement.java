package ru.hollowhorizon.hc.client.screens.util;

import net.minecraft.client.gui.components.AbstractWidget;
import ru.hollowhorizon.hc.client.screens.HollowScreen;
import ru.hollowhorizon.hc.client.screens.widget.IWidgetConsumer;

public class WidgetPlacement {
    private WidgetPlacement() {
    }

    public static <T extends AbstractWidget> T configureWidget(IWidgetConsumer<T> widget, Alignment alignment, int offsetX, int offsetY, int positionWidth, int positionHeight, int targetWidth, int targetHeight) {
        return configureWidget(widget, alignment, offsetX, offsetY, positionWidth, positionHeight, targetWidth, targetHeight, 1.0F);
    }

    public static <T extends AbstractWidget> T configureWidget(IWidgetConsumer<T> widget, Alignment alignment, int offsetX, int offsetY, int positionWidth, int positionHeight, int targetWidth, int targetHeight, float size) {
        int x = HollowScreen.getAlignmentPosX(alignment, offsetX, positionWidth, targetWidth, size);
        int y = HollowScreen.getAlignmentPosY(alignment, offsetY, positionHeight, targetHeight, size);

        return widget.create(x, y, targetWidth, targetHeight);
    }
}
