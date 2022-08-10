package ru.hollowhorizon.hc.core

import net.minecraftforge.fml.ModLoadingStage
import net.minecraftforge.fml.javafmlmod.FMLModContainer
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.ModFileScanData
import ru.hollowhorizon.hc.common.registry.HollowModProcessor

class HollowModContainer(info: IModInfo, className: String, modClassLoader: ClassLoader, private val scanData: ModFileScanData) : FMLModContainer(info, className, modClassLoader, scanData) {
    init {
        val runnable = activityMap[ModLoadingStage.CONSTRUCT]
        activityMap.put(ModLoadingStage.CONSTRUCT) {
            runnable?.run()
            hollowLoad()
        }
    }

    private fun hollowLoad() {
        HollowModProcessor.run(getModId(), scanData)
    }
}