package ru.hollowhorizon.hc.common.registry

import ru.hollowhorizon.hc.common.objects.blocks.SaveObeliskBlock

object ModBlocks : HollowRegistry() {
    val SAVE_OBELISK_BLOCK by register("save_obelisk_block") { SaveObeliskBlock() }
}
