package ru.hollowhorizon.hc.common.events.registry

import net.minecraft.server.packs.resources.PreparableReloadListener
import ru.hollowhorizon.hc.common.events.Event

class RegisterClientReloadListenersEvent : Event {
    val listeners = HashSet<PreparableReloadListener>()
    fun register(listener: PreparableReloadListener) {
        listeners += listener
    }
}