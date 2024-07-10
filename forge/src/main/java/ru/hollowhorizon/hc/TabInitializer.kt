package ru.hollowhorizon.hc

import net.minecraft.world.item.CreativeModeTab
import ru.hollowhorizon.hc.common.registry._tabCallback

object TabInitializer {
    @JvmStatic
    fun create() {
        val builder = CreativeModeTab.builder()
        _tabCallback = {
            builder.apply(it)
            builder.build()
        }
    }
}