package ru.hollowhorizon.hc.common.utils

//? if fabric {
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.network.chat.Component
//?}
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.entity.player.Inventory
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.level.Level
//? if forge {
/*import net.minecraftforge.common.extensions.IForgeMenuType
import net.minecraftforge.network.NetworkHooks
*///?}

fun <T> Player.openMenuScreen(provider: MenuProvider, level: Level, pos: BlockPos) where T: MenuProvider {
    if (!level.isClientSide) {
        this as ServerPlayer
        //? if fabric {
        this.openMenu(object : ExtendedScreenHandlerFactory {
            override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu? = provider.createMenu(i, inventory, player)

            override fun getDisplayName(): Component = provider.displayName

            override fun writeScreenOpeningData(p0: ServerPlayer?, buf: FriendlyByteBuf) {
                buf.writeBlockPos(pos)
            }
        })
        //?} elif forge
        /*NetworkHooks.openScreen(this, provider, pos)*/
    }
}

inline fun <reified T: AbstractContainerMenu> simpleMenuFactory(noinline factory: (Int, Inventory, FriendlyByteBuf) -> T): MenuType<T> =
    //? if forge {
    /*IForgeMenuType.create(factory::invoke)
    *///?} elif fabric
    ExtendedScreenHandlerType(factory::invoke)
