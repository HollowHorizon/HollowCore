package ru.hollowhorizon.hc.common.container;

import net.minecraft.client.gui.widget.button.Button;

import java.util.ArrayList;

public class ButtonManager {
    private final ArrayList<Button> buttons = new ArrayList<>();

    public <T extends Button> T addButton(T button) {
        this.buttons.add(button);
        return button;
    }

    public void clear() {
        this.buttons.clear();
    }

    public ArrayList<Button> getButtons() {
        return buttons;
    }
}
