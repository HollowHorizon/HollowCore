package ru.hollowhorizon.hc.common.objects.blocks.multi

import net.minecraft.block.Blocks
import net.minecraft.util.math.vector.Vector3d
import ru.hollowhorizon.hc.common.objects.blocks.multi.structure.MultiBlockBuilder
import ru.hollowhorizon.hc.common.objects.blocks.multi.structure.MultiBlockStructure

class MultiBlockSystem {
    companion object {
        val storage = mutableListOf<MultiBlockStructure>()

        @JvmStatic
        fun init() {
            storage.add(
                MultiBlockBuilder()
                    .layer {
                        line(Blocks.ANDESITE, Blocks.ANDESITE, Blocks.ANDESITE)
                    }
                    .layer {
                        line(Blocks.ANDESITE, Blocks.ANDESITE, Blocks.ANDESITE)
                    }
                    .layer {
                        line(Blocks.ANDESITE, Blocks.ANDESITE, Blocks.ANDESITE)
                    }
                    .configure {
                        modelName = "ultra_andesite"
                        offset = Vector3d(1.0, 0.0, 0.0)
                    }
                    .build()
            )
        }
    }
}