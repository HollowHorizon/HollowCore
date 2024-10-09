package ru.hollowhorizon.hc.common.registry

import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.material.Material
import ru.hollowhorizon.hc.common.objects.blocks.BlockItemProperties

object ModItems : HollowRegistry() {
    val TEST by register("example", autoModel = AutoModelType.CUBE_ALL) {
        ExampleBlock()
    }
}

class ExampleBlock: Block(Properties.of(Material.METAL)), BlockItemProperties {
    override val properties = Item.Properties()
}
