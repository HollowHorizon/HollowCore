package ru.hollowhorizon.hc.common.registry

import net.minecraft.world.item.Item
import net.minecraft.world.item.Tiers
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import ru.hollowhorizon.hc.common.events.tags.ToolTypes
import ru.hollowhorizon.hc.common.events.tags.addTool
import ru.hollowhorizon.hc.common.objects.blocks.BlockItemProperties
import ru.hollowhorizon.hc.common.objects.items.CreativeTab

object ModItems : HollowRegistry() {
    val EXAMPLE by register("example", autoModel = AutoModelType.CUBE_ALL) {
        Example()
    }

    val TAB by creativeTab("example")
}

class Example : Block(Properties.copy(Blocks.STONE)), BlockItemProperties, CreativeTab {
    init {
        addTool(ToolTypes.PICKAXE, Tiers.IRON)
    }

    override val properties: Item.Properties
        get() = Item.Properties()

    override fun tab() = ModItems.TAB
}