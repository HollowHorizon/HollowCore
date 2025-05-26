/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.common.utils

//? if fabric {
/*import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType
import net.minecraft.network.chat.Component
*///?}
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
import net.minecraftforge.common.extensions.IForgeMenuType
import net.minecraftforge.network.NetworkHooks
//?}

/**
 * Opens a menu screen for the player.
 *
 * @param provider The menu provider.
 * @param level The level where the menu is opened.
 * @param pos The position of the block related to the menu.
 */
fun <T> Player.openMenuScreen(provider: MenuProvider, level: Level, pos: BlockPos) where T: MenuProvider {
    if (!level.isClientSide) {
        this as ServerPlayer
        //? if fabric {
        /*this.openMenu(object : ExtendedScreenHandlerFactory {
            override fun createMenu(i: Int, inventory: Inventory, player: Player): AbstractContainerMenu? = provider.createMenu(i, inventory, player)

            override fun getDisplayName(): Component = provider.displayName

            override fun writeScreenOpeningData(p0: ServerPlayer?, buf: FriendlyByteBuf) {
                buf.writeBlockPos(pos)
            }
        })
        *///?} elif forge
        NetworkHooks.openScreen(this, provider, pos)
    }
}

/**
 * Creates a simple MenuType with a specified factory.
 *
 * @param factory The factory function to create the menu.
 * @return A new MenuType instance.
 */
inline fun <reified T: AbstractContainerMenu> simpleMenuFactory(noinline factory: (Int, Inventory, FriendlyByteBuf) -> T): MenuType<T> =
    //? if forge {
    IForgeMenuType.create(factory::invoke)
    //?} elif fabric
    /*ExtendedScreenHandlerType(factory::invoke)*/
