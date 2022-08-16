package ru.hollowhorizon.hc.client.screens.widget.button;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class MenuButton extends BaseButton {
    public MenuButton(int x, int y, int width, int height, ITextComponent text, BasePressable onPress) {
        super(x, y, width, height, text, onPress, new ResourceLocation("hc", "textures/gui/menu/hollow_core_menu_button.png"), 0xFFFFFF, 0xFFFFFF, new StringTextComponent("HollowCore Menu"));
    }
}
