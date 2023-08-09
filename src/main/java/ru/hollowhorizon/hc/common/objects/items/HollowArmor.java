package ru.hollowhorizon.hc.common.objects.items;

import net.minecraft.data.models.blockstates.PropertyDispatch;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import ru.hollowhorizon.hc.client.utils.HollowPack;

public class HollowArmor<T extends ArmorItem> {
    private final T helm;
    private final T chest;
    private final T legs;
    private final T boots;

    public HollowArmor(PropertyDispatch.TriFunction<ArmorMaterial, EquipmentSlot, Item.Properties, T> armor, ArmorMaterial material, Item.Properties properties) {
        
        this.helm = armor.apply(material, EquipmentSlot.HEAD, properties);
        this.chest = armor.apply(material, EquipmentSlot.CHEST, properties);
        this.legs = armor.apply(material, EquipmentSlot.LEGS, properties);
        this.boots = armor.apply(material, EquipmentSlot.FEET, properties);
    }

    public static <T extends ArmorItem> boolean isFullSet(LivingEntity entity, HollowArmor<T> armor) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = entity.getItemBySlot(EquipmentSlot.FEET);

        return head.getItem() == armor.helm && chest.getItem() == armor.chest && legs.getItem() == armor.legs && feet.getItem() == armor.boots;
    }

    public static <T extends ArmorItem> void damagePart(LivingEntity entity, EquipmentSlot target, int damage) {
        ItemStack armorItem = entity.getItemBySlot(target);

        armorItem.hurtAndBreak(damage, entity, (entity1 -> {}));
    }

    public static <T extends ArmorItem> boolean hasPart(LivingEntity entity, HollowArmor<T> armor, EquipmentSlot target) {
        ItemStack arm = entity.getItemBySlot(target);

        return arm.getItem() == armor.helm || arm.getItem() == armor.chest || arm.getItem() == armor.legs || arm.getItem() == armor.boots;
    }

    public static <T extends ArmorItem> boolean isContainsAnyPart(LivingEntity entity, HollowArmor<T> armor) {
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        ItemStack chest = entity.getItemBySlot(EquipmentSlot.CHEST);
        ItemStack legs = entity.getItemBySlot(EquipmentSlot.LEGS);
        ItemStack feet = entity.getItemBySlot(EquipmentSlot.FEET);

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
