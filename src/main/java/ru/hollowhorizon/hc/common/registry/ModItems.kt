package ru.hollowhorizon.hc.common.registry

import net.minecraft.world.item.Item
import ru.hollowhorizon.hc.client.utils.rl

object ModItems : HollowRegistry() {
    val JOKE by register("hollowcore:joke".rl, true) {
        Item(Item.Properties())
    }
}