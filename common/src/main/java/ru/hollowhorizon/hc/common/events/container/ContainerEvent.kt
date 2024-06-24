package ru.hollowhorizon.hc.common.events.container

import ru.hollowhorizon.hc.common.capabilities.containers.HollowContainer
import ru.hollowhorizon.hc.common.events.Cancelable
import ru.hollowhorizon.hc.common.events.Event

open class ContainerEvent(val container: HollowContainer): Event, Cancelable {
    override var isCanceled = false
    class OnTake(container: HollowContainer, val slot: Int): ContainerEvent(container)
}