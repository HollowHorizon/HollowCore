package ru.hollowhorizon.hc.client.screens.widget.button;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class MenuButton extends BaseButton {
    public MenuButton(int x, int y, int width, int height, ITextComponent text, IPressable onPress) {
        super(x, y, width, height, text, onPress, new ResourceLocation("hc", "textures/gui/menu/hollow_core_menu_button.png"));
    }
}
