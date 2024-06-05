package ru.hollowhorizon.hc.common.registry

import ru.hollowhorizon.hc.common.capabilities.CAPABILITIES
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.capabilities.LoadCapabilitiesEvent

object ModCapabilities {
    @SubscribeEvent
    fun loadCapabilities(event: LoadCapabilitiesEvent) {
        CAPABILITIES.filter { it.key.isInstance(event.provider) }.forEach {
            it.value.forEach {
                event.addCapability(it())
            }
        }
    }
}