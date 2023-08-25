package ru.hollowhorizon.hc.common.registry

import net.minecraft.world.level.block.entity.BlockEntityType
import ru.hollowhorizon.hc.common.objects.tiles.SaveObeliskTile

object ModTileEntities : HollowRegistry() {
    val SAVE_OBELISK_TILE by register {
        BlockEntityType.Builder.of(
            ::SaveObeliskTile,
            ModBlocks.SAVE_OBELISK_BLOCK.get()
        ).build(promise())
    }
}