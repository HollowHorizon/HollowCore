package ru.hollowhorizon.hc.common.objects.items

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.Item
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.RegistryObject
import ru.hollowhorizon.hc.client.utils.HollowPack

class HollowArmor<T : ArmorItem>(
    armor: (ArmorMaterial, EquipmentSlot, Item.Properties) -> T,
    material: ArmorMaterial,
    properties: Item.Properties,
) {
    val helmet: () -> T = { armor(material, EquipmentSlot.HEAD, properties) }
    val chest: () -> T = { armor(material, EquipmentSlot.CHEST, properties) }
    val legs: () -> T = { armor(material, EquipmentSlot.LEGS, properties) }
    val boots: () -> T = { armor(material, EquipmentSlot.FEET, properties) }

    fun registerItems(register: DeferredRegister<Item>, name: String): RegistryObject<T> {
        register.register(name + "_boots", boots)
        register.register(name + "_legs", legs)
        register.register(name + "_chest", chest)
        return register.register(name + "_helmet", helmet)
    }

    fun registerModels(modid: String, name: String) {
        HollowPack.genItemModels.add(ResourceLocation(modid, name + "_helmet"))
        HollowPack.genItemModels.add(ResourceLocation(modid, name + "_chest"))
        HollowPack.genItemModels.add(ResourceLocation(modid, name + "_legs"))
        HollowPack.genItemModels.add(ResourceLocation(modid, name + "_boots"))
    }

    companion object {
        fun isFullSet(entity: LivingEntity, armor: HollowArmor<*>): Boolean {
            val head = entity.getItemBySlot(EquipmentSlot.HEAD)
            val chest = entity.getItemBySlot(EquipmentSlot.CHEST)
            val legs = entity.getItemBySlot(EquipmentSlot.LEGS)
            val feet = entity.getItemBySlot(EquipmentSlot.FEET)
            return head.item === armor.helmet && chest.item === armor.chest && legs.item === armor.legs && feet.item === armor.boots
        }

        fun damagePart(entity: LivingEntity, target: EquipmentSlot, damage: Int) {
            val armorItem = entity.getItemBySlot(target)
            armorItem.hurtAndBreak(damage, entity) { _ -> }
        }

        fun hasPart(entity: LivingEntity, armor: HollowArmor<*>, target: EquipmentSlot?): Boolean {
            val arm = entity.getItemBySlot(target!!)
            return arm.item === armor.helmet || arm.item === armor.chest || arm.item === armor.legs || arm.item === armor.boots
        }

        fun isContainsAnyPart(entity: LivingEntity, armor: HollowArmor<*>): Boolean {
            val head = entity.getItemBySlot(EquipmentSlot.HEAD)
            val chest = entity.getItemBySlot(EquipmentSlot.CHEST)
            val legs = entity.getItemBySlot(EquipmentSlot.LEGS)
            val feet = entity.getItemBySlot(EquipmentSlot.FEET)
            return head.item === armor.helmet || chest.item === armor.chest || legs.item === armor.legs || feet.item === armor.boots
        }
    }
}
