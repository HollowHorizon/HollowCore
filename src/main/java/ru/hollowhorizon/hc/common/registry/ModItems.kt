package ru.hollowhorizon.hc.common.registry

import net.minecraft.world.item.Item

object ModItems : HollowRegistry() {
    val JOKE by register("joke", AutoModelType.DEFAULT) {
        Item(Item.Properties())
    }
}