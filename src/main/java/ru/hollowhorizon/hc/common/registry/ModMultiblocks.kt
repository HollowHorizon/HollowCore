package ru.hollowhorizon.hc.common.registry

import net.minecraft.world.level.block.Blocks
import ru.hollowhorizon.hc.common.multiblock.Multiblock
import ru.hollowhorizon.hc.common.multiblock.Reg

object ModMultiblocks : HollowRegistry() {
    val mithrilineFurnace by register("mithriline_furnace") {
        val core = Reg.example.get().defaultBlockState()
        val plate = Blocks.COBBLESTONE.defaultBlockState()
        val diamondBlock = Blocks.DIAMOND_BLOCK.defaultBlockState()
        Multiblock {
            size(5, 5, 3)
            pattern(
                plate, null, plate, null, plate,
                null, plate, plate, plate, null,
                plate, plate, null, plate, plate,
                null, plate, plate, plate, null,
                plate, null, plate, null, plate,

                plate, null, plate, null, plate,
                null, null, diamondBlock, null, null,
                plate, null, core, null, plate,
                null, null, null, diamondBlock, null,
                plate, null, plate, null, plate,

                plate, null, null, null, plate,
                null, null, null, null, null,
                null, null, null, null, null,
                null, null, null, null, null,
                plate, null, null, null, plate,
            )
        }
    }
}
