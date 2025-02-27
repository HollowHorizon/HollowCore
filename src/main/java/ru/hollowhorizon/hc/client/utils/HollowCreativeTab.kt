package ru.hollowhorizon.hc.client.utils

//? if fabric
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup
//? if forge
/*import net.minecraft.world.item.CreativeModeTab*/

object HollowCreativeTab {
    @JvmStatic
    fun builder() =
        //? if fabric
        FabricItemGroup.builder()
        //? if forge
        /*CreativeModeTab.builder()*/
}