package ru.hollowhorizon.hc.common.events.entity.player

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.common.events.Event

open class PlayerEvent(val player: Player) : Event {
    class Clone(player: Player, val oldPlayer: Player, val wasDeath: Boolean) : PlayerEvent(player)

    class Join(player: Player) : PlayerEvent(player)
    class ChangeDimension(player: Player, val from: ServerLevel, val to: ServerLevel) : PlayerEvent(player)
}