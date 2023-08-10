package ru.hollowhorizon.hc.common.registry

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.entity.BlockEntityType.BlockEntitySupplier
import net.minecraft.world.level.block.state.BlockState
import ru.hollowhorizon.hc.api.registy.HollowRegister
import ru.hollowhorizon.hc.common.objects.tiles.SaveObeliskTile

object ModTileEntities: HollowRegistry() {
    val SAVE_OBELISK_TILE by register {
        BlockEntityType.Builder.of(
            ::SaveObeliskTile,
            ModBlocks.SAVE_OBELISK_BLOCK.get()
        ).build(promise())
    }
}
