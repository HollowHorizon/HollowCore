package ru.hollowhorizon.hc.common.registry;

import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import ru.hollowhorizon.hc.api.registy.HollowRegister;

public class ModItems {
    @HollowRegister(auto_model = true)
    public static Item NEW_ITEM = new Item(new Item.Properties().tab(ItemGroup.TAB_MISC));
}
