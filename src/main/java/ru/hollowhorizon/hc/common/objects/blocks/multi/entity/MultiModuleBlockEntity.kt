package ru.hollowhorizon.hc.common.objects.blocks.multi.entity

import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.math.BlockPos
import ru.hollowhorizon.hc.common.objects.tiles.HollowTileEntity
import ru.hollowhorizon.hc.common.registry.ModTileEntities

class MultiModuleBlockEntity: HollowTileEntity(ModTileEntities.MULTI_MODULE_BLOCK_ENTITY) {
    var corePos: BlockPos? = null

    fun breakMultiBlock() {
        if (level == null || corePos == null) return

        level!!.getBlockEntity(corePos!!)?.let {
            val tile = it as MultiCoreBlockEntity

            if(!tile.isDestroying) {
                tile.breakMultiBlock()
            }
        }
    }

    override fun saveNBT(nbt: CompoundNBT) {
        if (corePos != null) {
            nbt.putInt("coreX", corePos!!.x)
            nbt.putInt("coreY", corePos!!.y)
            nbt.putInt("coreZ", corePos!!.z)
        }
    }

    override fun loadNBT(nbt: CompoundNBT) {
        if (nbt.contains("coreX") && nbt.contains("coreY") && nbt.contains("coreZ")) {
            corePos = BlockPos(nbt.getInt("coreX"), nbt.getInt("coreY"), nbt.getInt("coreZ"))
        }
    }
}