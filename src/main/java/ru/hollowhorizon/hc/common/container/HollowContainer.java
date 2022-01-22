package ru.hollowhorizon.hc.common.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.world.World;
import net.minecraftforge.common.extensions.IForgeContainerType;
import ru.hollowhorizon.hc.api.recipes.HollowRecipe;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.common.container.slots.HollowResultSlot;

public class HollowContainer extends Container {
    @HollowRegister
    public static final ContainerType<HollowContainer> hollow_container = IForgeContainerType.create((windowId, inv, data) -> new HollowContainer(windowId, inv));

    private final IInventory inventory;
    private final World level;

    public HollowContainer(int windowId, PlayerInventory playerInventoryIn) {
        this(windowId, playerInventoryIn, new Inventory(3));
    }

    public HollowContainer(int windowId, PlayerInventory playerInventoryIn, IInventory inv) {
        super(hollow_container, windowId);

        this.inventory = inv;
        this.level = playerInventoryIn.player.level;
        this.addSlot(new Slot(inv, 0, 56, 17));
        this.addSlot(new Slot(inv, 1, 56, 53));
        this.addSlot(new HollowResultSlot(playerInventoryIn.player, inv, 2, 116, 35));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventoryIn, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventoryIn, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean stillValid(PlayerEntity p_75145_1_) {
        return true;
    }

    public boolean matches(IRecipe<? super IInventory> recipeIn) {
        return recipeIn.matches(this.inventory, this.level);
    }
}
