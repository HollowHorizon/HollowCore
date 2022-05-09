package ru.hollowhorizon.hc.common.container;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeType;
import ru.hollowhorizon.hc.common.container.inventory.HollowInventory;

import java.util.ArrayList;
import java.util.List;

public class ContainerManager {
    private final HollowContainer container;
    private final ServerPlayerEntity player;
    private final ArrayList<HollowInventory> inventories = new ArrayList<>();
    private final ArrayList<Slot> playerSlots = new ArrayList<>();

    public ContainerManager(HollowContainer container, ServerPlayerEntity player) {
        this.player = player;
        this.container = container;
    }

    public <T extends HollowInventory> T addInventory(T inventory, Slot... slots) {
        inventory.setSlots(slots);
        this.inventories.add(inventory);
        inventory.setContainer(this.container);
        return inventory;
    }

    public void addPlayerInventory(int startX, int startY) {
        ArrayList<Slot> slots = new ArrayList<>();
        for (int l = 0; l < 3; ++l) {
            for (int j1 = 0; j1 < 9; ++j1) {
                slots.add(new Slot(this.player.inventory, j1 + (l + 1) * 9, startX + j1 * 18, startY + l * 18));
            }
        }
        playerSlots.addAll(slots);
    }

    public <C extends IInventory, T extends IRecipe<C>> void addRecipeListener(C input, HollowInventory output, IRecipeType<T> recipeType) {
        if(getContainer()!=null) {
            List<T> recipes = getContainer().getWorld().getRecipeManager().getAllRecipesFor(recipeType);
            for(T recipe : recipes) {
                if(recipe.matches(input, getContainer().getWorld())) {
                    ItemStack stack = recipe.assemble(input);
                    output.setItem(0, stack);
                }
            }
        }
    }

    public void addPlayerHotBar(int startX, int startY) {
        ArrayList<Slot> slots = new ArrayList<>();
        for (int i1 = 0; i1 < 9; ++i1) {
            slots.add(new Slot(this.player.inventory, i1, startX + i1 * 18, startY));
        }
        playerSlots.addAll(slots);
    }

    public HollowContainer getContainer() {
        return container;
    }

    public ArrayList<HollowInventory> getInventories() {
        return inventories;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }
}
