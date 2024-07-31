package ru.hollowhorizon.hc.common.events

import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.common.coroutines.onMainThreadSync
import ru.hollowhorizon.hc.common.coroutines.scopeSync
import ru.hollowhorizon.hc.common.events.entity.player.PlayerEvent
import ru.hollowhorizon.hc.common.events.server.ServerEvent
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

object EventBus {
    val listeners = hashMapOf<KClass<out Event>, MutableList<EventListener<out Event>>>()

    inline fun <reified T : Event> register(listener: EventListener<T>) {
        scopeSync {
            val list = listeners.getOrPut(T::class) { mutableListOf() }
            list.add(listener)
            list.sortBy { it.priority }
        }
    }

    fun registerNoInline(type: Class<Event>, listener: EventListener<Event>) {
        scopeSync {
            val list = listeners.getOrPut(type.kotlin) { mutableListOf() }
            list.add(listener)
            list.sortBy { it.priority }
        }
    }

    inline fun <reified T : Event> unregister(listener: EventListener<T>) {
        listeners[T::class]?.remove(listener)
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T : Event> post(event: T) {
        val cancelable = event as? Cancelable

        listeners.computeIfAbsent(event::class) { mutableListOf() }
            .forEach {
                (it as EventListener<T>).onEvent(event)
                if (cancelable?.isCanceled == true) return
            }
    }
}

suspend inline fun <reified T : Event> awaitEvent(): T {
    var listener: EventListener<T>? = null

    val result: T = suspendCoroutine { continuation ->

        listener = EventListener { event ->
            scopeSync {
                onMainThreadSync {
                    continuation.resume(event)
                }
            }
        }
        EventBus.register(listener ?: return@suspendCoroutine)

    }

    scopeSync {
        EventBus.unregister(listener ?: return@scopeSync)
    }

    return result
}

suspend fun greeting(message: Component) {
    val event: ServerEvent.Started = awaitEvent() // Ждём запуска сервера

    val players = awaitEvent<PlayerEvent.Join>() // Ждём хоть 1 игрока

    players.player.sendSystemMessage(message)
}