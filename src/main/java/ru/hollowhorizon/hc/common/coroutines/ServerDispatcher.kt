package ru.hollowhorizon.hc.common.coroutines

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import net.minecraft.server.MinecraftServer

interface ServerDispatcher {
    val `hollowcore$dispatcher`: CoroutineDispatcher
    val `hollowcore$coroutineScope`: CoroutineScope
}

private val MinecraftServer.ext get() = this as ServerDispatcher
val MinecraftServer.dispatcher get() = ext.`hollowcore$dispatcher`
val MinecraftServer.coroutineScope get() = ext.`hollowcore$coroutineScope`