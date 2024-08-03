package ru.hollowhorizon.hc.common.coroutines

import kotlinx.coroutines.*
import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.server.ServerEvent

internal lateinit var mcCoroutineDispatcher: CoroutineDispatcher
lateinit var mcCoroutineScope: CoroutineScope
internal val hollowCoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

fun scopeSync(block: suspend CoroutineScope.() -> Unit) = hollowCoroutineScope.launch(block = block)
fun scopeAsync(block: suspend CoroutineScope.() -> Unit) = hollowCoroutineScope.launch(block = block)

val clientDispatcher by lazy { Minecraft.getInstance().asCoroutineDispatcher() }

fun CoroutineScope.onMainThreadSync(block: suspend CoroutineScope.() -> Unit): Job {
    val dispatcher = if (isServerLoaded) mcCoroutineDispatcher else clientDispatcher
    return launch(dispatcher, block = block)
}

fun <T> CoroutineScope.onMainThreadAsync(block: suspend CoroutineScope.() -> T): Deferred<T> {
    val dispatcher = if (isServerLoaded) mcCoroutineDispatcher else clientDispatcher
    return async(dispatcher, block = block)
}

private fun waiter(checker: CompletableDeferred<Boolean>, condition: () -> Boolean) {
    mcCoroutineScope.onMainThreadSync {
        if (condition()) checker.complete(true)
        else {
            delay(50L)
            waiter(checker, condition)
        }
    }
}

suspend fun suspendBy(condition: () -> Boolean) {
    val checker = CompletableDeferred<Boolean>()

    waiter(checker, condition)

    checker.await()
}

var isServerLoaded = false

@SubscribeEvent
fun onServerStart(event: ServerEvent.Started) {
    mcCoroutineDispatcher = event.server.asCoroutineDispatcher()
    mcCoroutineScope = CoroutineScope(SupervisorJob() + mcCoroutineDispatcher)
    isServerLoaded = true
}