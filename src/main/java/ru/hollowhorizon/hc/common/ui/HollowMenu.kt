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

package ru.hollowhorizon.hc.common.ui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.SimpleMenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.network.NetworkHooks
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.common.registry.ModMenus.HOLLOW_MENU
import kotlin.random.Random


class HollowMenu(val id: Int, val playerInventory: Inventory) : AbstractContainerMenu(HOLLOW_MENU.get(), id) {
    init {
        if(Random.nextBoolean()) {
            for (k in 0..2) {
                for (i1 in 0..8) {
                    this.addSlot(Slot(playerInventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18))
                }
            }

            for (l in 0..8) {
                this.addSlot(Slot(playerInventory, l, 8 + l * 18, 142))
            }
        }
    }

    override fun quickMoveStack(pPlayer: Player, pIndex: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun stillValid(pPlayer: Player) = true
}

class HollowMenuScreen(menu: HollowMenu) : AbstractContainerScreen<HollowMenu>(menu, menu.playerInventory, "".mcText) {
    constructor(menu: HollowMenu, inventory: Inventory, component: Component) : this(menu)

    override fun renderBg(pPoseStack: PoseStack, pPartialTick: Float, pMouseX: Int, pMouseY: Int) {
        renderBackground(pPoseStack)
    }

}

fun register(event: FMLClientSetupEvent) {
    event.enqueueWork {
        MenuScreens.register(
            HOLLOW_MENU.get(),
            ::HollowMenuScreen
        )
    }
}

fun ((Int, Inventory) -> HollowMenu).open(player: ServerPlayer) {
    NetworkHooks.openScreen(
        player, SimpleMenuProvider(
            { containerId: Int, playerInventory: Inventory, _: Player ->
                this(containerId, playerInventory)
            },
            "".mcText
        )
    )
}