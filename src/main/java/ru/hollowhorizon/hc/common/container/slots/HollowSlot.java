package ru.hollowhorizon.hc.common.container.slots;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import ru.hollowhorizon.hc.common.container.HollowContainer;

public class HollowSlot extends Slot {
    private final HollowContainer field_216939_a;

    public HollowSlot(HollowContainer p_i50084_1_, IInventory p_i50084_2_, int p_i50084_3_, int p_i50084_4_, int p_i50084_5_) {
        super(p_i50084_2_, p_i50084_3_, p_i50084_4_, p_i50084_5_);
        this.field_216939_a = p_i50084_1_;
    }
}
