package ru.hollowhorizon.hc.common.objects.blocks.multi.entity

import net.minecraft.entity.player.PlayerEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.vector.Vector3d
import ru.hollowhorizon.hc.common.objects.blocks.multi.structure.MultiBlockStorage
import ru.hollowhorizon.hc.common.objects.tiles.HollowTileEntity
import ru.hollowhorizon.hc.common.registry.ModTileEntities

class MultiCoreBlockEntity() : HollowTileEntity(ModTileEntities.MULTI_CORE_BLOCK_ENTITY) {
    var onOpen: (PlayerEntity, BlockPos) -> Unit = {_, _ -> }
    var name = "3d_print"
    var offset = Vector3d(1.0, 0.0, 0.0)
    var modules: MutableList<BlockPos> = mutableListOf()
    var isDestroying = false

    fun breakMultiBlock() {
        if (level == null) return
        isDestroying = true

        modules.forEach {
            level!!.destroyBlock(it, false)
        }

        level!!.destroyBlock(worldPosition, false)
    }

    override fun saveNBT(nbt: CompoundNBT) {
        if (modules.isNotEmpty()) {
            nbt.putInt("modulesCount", modules.size)
            for (i in modules.indices) {
                nbt.putInt("moduleX$i", modules[i].x)
                nbt.putInt("moduleY$i", modules[i].y)
                nbt.putInt("moduleZ$i", modules[i].z)
            }
        }
        nbt.putString("name", name)
        nbt.putDouble("offsetX", offset.x)
        nbt.putDouble("offsetY", offset.y)
        nbt.putDouble("offsetZ", offset.z)
    }

    override fun loadNBT(nbt: CompoundNBT) {
        if (nbt.contains("modulesCount")) {
            val modulesCount = nbt.getInt("modulesCount")
            for (i in 0 until modulesCount) {
                modules.add(BlockPos(nbt.getInt("moduleX$i"), nbt.getInt("moduleY$i"), nbt.getInt("moduleZ$i")))
            }
        }
        name = nbt.getString("name")
        onOpen = MultiBlockStorage.multiBlockActions[name] ?: { _, _ -> }
        offset = Vector3d(nbt.getDouble("offsetX"), nbt.getDouble("offsetY"), nbt.getDouble("offsetZ"))

    }

    //Here GECOLIB code

    override fun getRenderBoundingBox(): AxisAlignedBB {
        return INFINITE_EXTENT_AABB
    }
}