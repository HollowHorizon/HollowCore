package ru.hollowhorizon.hc.common.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class TestUContainer extends UniversalContainer {
    @Override
    public ITextComponent containerName() {
        return new StringTextComponent("TestUContainer");
    }

    @Override
    public void serverInit(HollowContainer container) {
        container.createSlot(new Slot(new Inventory(1), 0, 0, 0));
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY) {
        super.render(stack, mouseX, mouseY);
    }
}
