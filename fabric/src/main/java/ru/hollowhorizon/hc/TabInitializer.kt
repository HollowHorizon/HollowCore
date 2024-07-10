package ru.hollowhorizon.hc

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import ru.hollowhorizon.hc.common.registry._tabCallback

object TabInitializer {
    @JvmStatic
    fun create() {
        val builder = FabricItemGroup.builder()
        _tabCallback = {
            builder.apply(it)
            builder.build()
        }
    }
}