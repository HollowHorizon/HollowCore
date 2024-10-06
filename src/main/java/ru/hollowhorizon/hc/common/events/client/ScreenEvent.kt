package ru.hollowhorizon.hc.common.events.client

import net.minecraft.client.gui.screens.Screen
import ru.hollowhorizon.hc.common.events.Event

open class ScreenEvent(var screen: Screen): Event {
    class Open(screen: Screen): ScreenEvent(screen)
}