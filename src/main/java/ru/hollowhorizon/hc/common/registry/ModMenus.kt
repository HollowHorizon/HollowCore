package ru.hollowhorizon.hc.common.registry

import net.minecraft.world.inventory.MenuType
import ru.hollowhorizon.hc.common.ui.HollowMenu

object ModMenus: HollowRegistry() {
    val HOLLOW_MENU by register("hollow_menu") {
        MenuType(::HollowMenu)
    }
}