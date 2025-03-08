package ru.hollowhorizon.hc.common.utils

//? if fabric
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
import net.minecraft.world.item.CreativeModeTab

object HollowCreativeTab {
    @JvmStatic
    fun builder(): CreativeModeTab.Builder =
        //? if fabric
        FabricItemGroup.builder()
        //? if forge
        /*CreativeModeTab.builder()*/
}