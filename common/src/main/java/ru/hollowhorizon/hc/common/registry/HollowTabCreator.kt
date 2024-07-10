package ru.hollowhorizon.hc.common.registry

import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.ItemStack
import java.util.function.Consumer

lateinit var _tabCallback: (CreativeModeTab.Builder.() -> Unit) -> CreativeModeTab

object HollowTabCreator {
    @JvmStatic
    fun create(title: Component, icon: () -> ItemStack) = create {
        this.title(title)
        this.icon(icon)
    }

    @JvmStatic
    fun create(callback: CreativeModeTab.Builder.() -> Unit): CreativeModeTab =
        _tabCallback(callback)
}