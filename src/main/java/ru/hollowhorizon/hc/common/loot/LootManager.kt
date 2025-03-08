package ru.hollowhorizon.hc.common.loot

import net.minecraft.data.loot.BlockLootSubProvider
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.storage.loot.LootPool
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.LootItem
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterLootEvent

object LootManager {
    val BLOCK_DROPS = HashMap<Block, () -> LootTable>()
    val ENTITY_DROPS = HashMap<EntityType<*>, () -> LootTable>()

    @SubscribeEvent
    fun onReload(event: RegisterLootEvent) {
        BLOCK_DROPS.forEach { (block, loot) ->
            event.addLoot(block, loot())
        }
        ENTITY_DROPS.forEach { (block, loot) ->
            event.addLoot(block, loot())
        }
    }
}

fun EntityType<*>.addDrop(table: () -> LootTable) {
    LootManager.ENTITY_DROPS[this] = table
}

fun Block.addDrop(table: () -> LootTable) {
    LootManager.BLOCK_DROPS[this] = table
}

fun Block.addDrop(item: Item? = null, silkTouch: Boolean = false) {
    LootManager.BLOCK_DROPS[this] = {
        if(silkTouch) BlockLootSubProvider.createSilkTouchOnlyTable(item ?: this.asItem()).build()
        else LootTable.lootTable()
            .setParamSet(LootContextParamSets.BLOCK)
            .pool(
                LootPool.lootPool()
                    .setRolls(ConstantValue.exactly(1f))
                    .setBonusRolls(ConstantValue.exactly(0f))
                    .add(LootItem.lootTableItem { item ?: this.asItem() })
                    .build()
            )
            .build()
    }
}

fun Block.addOreDrop(item: Item, explosionResistant: Boolean = false) {
    LootManager.BLOCK_DROPS[this] = {
        val loot = LootItem.lootTableItem(item).apply(ApplyBonusCount.addOreBonusCount(Enchantments.BLOCK_FORTUNE))
        if (explosionResistant) loot.apply(ApplyExplosionDecay.explosionDecay())
        BlockLootSubProvider.createSilkTouchDispatchTable(
            this,
            loot
        ).build()
    }
}