package ru.hollowhorizon.hc.common.network

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.common.events.Event
import ru.hollowhorizon.hc.common.events.awaitEvent
import ru.hollowhorizon.hc.common.events.post
//? if <=1.19.2
import ru.hollowhorizon.hc.client.utils.math.level

abstract class RequestPacket<T : RequestPacket<T>> : HollowPacketV3<T>, Event {
    override fun handle(player: Player) {
        if (player.level().isClientSide) handleClient(player)
        else handleServer(player)
    }

    private fun handleClient(player: Player) {
        this.post()
    }

    private fun handleServer(player: Player) {
        retrieveValue(player as ServerPlayer)
        this.send(player)
    }

    protected abstract fun retrieveValue(player: ServerPlayer)
}

suspend inline fun <reified T : RequestPacket<T>> T.request(): T {
    send()
    return awaitEvent<T>()
}