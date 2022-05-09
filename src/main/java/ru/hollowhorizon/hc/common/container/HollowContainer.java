package ru.hollowhorizon.hc.common.container;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.common.container.inventory.HollowInventory;

public abstract class HollowContainer {
    protected World world;

    @OnlyIn(Dist.CLIENT)
    public void clientInit(ScreenManager manager) {}
    @OnlyIn(Dist.CLIENT)
    public void onMouseEvent(MouseManager manager) {}
    @OnlyIn(Dist.CLIENT)
    public void onKeyEvent(KeyManager manager) {}

    @OnlyIn(Dist.DEDICATED_SERVER)
    public abstract void serverInit(ContainerManager manager);

    public void onItemRemoved(HollowInventory inventory, ItemStack removedItem, int slotId, int count) {}
    public void onItemUpdate(HollowInventory inventory, ItemStack item, int slotId) {}
    public void onInventoryClear(HollowInventory inventory) {}

    @OnlyIn(Dist.CLIENT)
    public abstract void render(MatrixStack stack, int mouseX, int mouseY);

    @OnlyIn(Dist.CLIENT)
    void blit(MatrixStack stack, int x, int y, int width, int height) {
        AbstractGui.blit(stack, x, y, 0,0, width, height, width, height);
    }

    public World getWorld() {
        return world;
    }
}
