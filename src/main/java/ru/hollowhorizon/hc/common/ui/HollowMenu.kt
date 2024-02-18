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