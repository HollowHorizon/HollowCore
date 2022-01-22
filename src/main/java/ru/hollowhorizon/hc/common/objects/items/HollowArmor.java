package ru.hollowhorizon.hc.common.objects.items;

import net.minecraft.data.BlockStateVariantBuilder;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import ru.hollowhorizon.hc.client.utils.HollowPack;

public class HollowArmor<T extends ArmorItem> {
    private final T helm;
    private final T chest;
    private final T legs;
    private final T boots;

    public HollowArmor(BlockStateVariantBuilder.ITriFunction<IArmorMaterial, EquipmentSlotType, Item.Properties, T> armor, IArmorMaterial material, Item.Properties properties) {
        this.helm = armor.apply(material, EquipmentSlotType.HEAD, properties);
        this.chest = armor.apply(material, EquipmentSlotType.CHEST, properties);
        this.legs = armor.apply(material, EquipmentSlotType.LEGS, properties);
        this.boots = armor.apply(material, EquipmentSlotType.FEET, properties);
    }

    public static <T extends ArmorItem> boolean isFullSet(LivingEntity entity, HollowArmor<T> armor) {
        ItemStack head = entity.getItemBySlot(EquipmentSlotType.HEAD);
        ItemStack chest = entity.getItemBySlot(EquipmentSlotType.CHEST);
        ItemStack legs = entity.getItemBySlot(EquipmentSlotType.LEGS);
        ItemStack feet = entity.getItemBySlot(EquipmentSlotType.FEET);

        return head.getItem() == armor.helm && chest.getItem() == armor.chest && legs.getItem() == armor.legs && feet.getItem() == armor.boots;
    }

    public static <T extends ArmorItem> void damagePart(LivingEntity entity, EquipmentSlotType target, int damage) {
        ItemStack armorItem = entity.getItemBySlot(target);

        armorItem.hurtAndBreak(damage, entity, (entity1 -> {}));
    }

    public static <T extends ArmorItem> boolean hasPart(LivingEntity entity, HollowArmor<T> armor, EquipmentSlotType target) {
        ItemStack arm = entity.getItemBySlot(target);

        return arm.getItem() == armor.helm || arm.getItem() == armor.chest || arm.getItem() == armor.legs || arm.getItem() == armor.boots;
    }

    public static <T extends ArmorItem> boolean isContainsAnyPart(LivingEntity entity, HollowArmor<T> armor) {
        ItemStack head = entity.getItemBySlot(EquipmentSlotType.HEAD);
        ItemStack chest = entity.getItemBySlot(EquipmentSlotType.CHEST);
        ItemStack legs = entity.getItemBySlot(EquipmentSlotType.LEGS);
        ItemStack feet = entity.getItemBySlot(EquipmentSlotType.FEET);

        return head.getItem() == armor.helm || chest.getItem() == armor.chest || legs.getItem() == armor.legs || feet.getItem() == armor.boots;
    }

    public void registerItems(DeferredRegister<Item> register, String name) {
        register.register(name + "_helmet", () -> helm);
        register.register(name + "_chest", () -> chest);
        register.register(name + "_legs", () -> legs);
        register.register(name + "_boots", () -> boots);
    }

    public void registerModels(String modid, String name) {
        HollowPack.genItemModels.add(new ResourceLocation(modid, name+"_helmet"));
        HollowPack.genItemModels.add(new ResourceLocation(modid, name+"_chest"));
        HollowPack.genItemModels.add(new ResourceLocation(modid, name+"_legs"));
        HollowPack.genItemModels.add(new ResourceLocation(modid, name+"_boots"));
    }

}
