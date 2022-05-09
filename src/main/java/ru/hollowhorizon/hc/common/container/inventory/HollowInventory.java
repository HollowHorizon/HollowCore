package ru.hollowhorizon.hc.common.container.inventory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import ru.hollowhorizon.hc.common.container.HollowContainer;

import java.util.ArrayList;
import java.util.List;

public class HollowInventory implements IInventory {
    private Slot[] slots = new Slot[0];
    private HollowContainer manager;

    public HollowInventory() {}

    public void setContainer(HollowContainer manager) {
        this.manager = manager;
    }

    public void setSlots(Slot... slots) {
        this.slots = slots;
    }

    @Override
    public int getContainerSize() {
        return this.slots.length;
    }

    @Override
    public boolean isEmpty() {
        for (Slot slot : slots) {
            if (slot.getItem().equals(ItemStack.EMPTY)) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slotId) {
        if (slotId < this.slots.length) {
            return this.slots[slotId].getItem();
        } else {
            throw new ArrayIndexOutOfBoundsException("trying get item from slot: " + slotId);
        }
    }

    @Override
    public ItemStack removeItem(int slotId, int count) {
        ArrayList<ItemStack> items = new ArrayList<>();
        for (Slot slot : slots) {
            items.add(slot.getItem());
        }
        ItemStack itemstack = ItemStackHelper.removeItem(items, slotId, count);
        if (this.manager != null) manager.onItemRemoved(this, itemstack, slotId, count);

        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotId) {
        ItemStack itemstack = slots[slotId].getItem();
        if (itemstack.isEmpty()) {
            return ItemStack.EMPTY;
        } else {
            slots[slotId].set(ItemStack.EMPTY);
            return itemstack;
        }
    }

    @Override
    public void setItem(int slotId, ItemStack itemStack) {
        if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }
        if (this.manager != null) this.manager.onItemUpdate(this, itemStack, slotId);
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (Slot slot : slots) {
            slot.set(ItemStack.EMPTY);
        }
        if (this.manager != null) this.manager.onInventoryClear(this);
    }

    public <C extends IInventory, T extends IRecipe<C>> boolean canCraft(IRecipeType<T> crafting) {
        if(this.manager!=null) {
            List<T> recipes = this.manager.getWorld().getRecipeManager().getAllRecipesFor(crafting);
            for(T recipe : recipes) {
                if(recipe.matches((C) this, this.manager.getWorld())) return true;
            }
        }
        return false;
    }

    public void craft(IRecipeType<ICraftingRecipe> crafting, HollowInventory output) {
    }
}
