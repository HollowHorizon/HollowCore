package ru.hollowhorizon.hc.common.container.slots;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import ru.hollowhorizon.hc.common.container.HollowContainer;

public class HollowResultSlot extends Slot {
    private final PlayerEntity player;
    private int removeCount;

    public HollowResultSlot(PlayerEntity player, IInventory inventoryIn, int slotIndex, int xPosition, int yPosition) {
        super(inventoryIn, slotIndex, xPosition, yPosition);
        this.player = player;
    }

    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    public ItemStack remove(int amount) {
        if (this.hasItem()) {
            this.removeCount += Math.min(amount, this.getItem().getCount());
        }

        return super.remove(amount);
    }

    public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
        this.onCrafting(stack);
        super.onTake(thePlayer, stack);
        return stack;
    }

    protected void onQuickCraft(ItemStack stack, int amount) {
        this.removeCount += amount;
        this.onCrafting(stack);
    }

    protected void onCrafting(ItemStack stack) {
        stack.onCraftedBy(this.player.level, this.player, this.removeCount);

        this.removeCount = 0;
    }
}
