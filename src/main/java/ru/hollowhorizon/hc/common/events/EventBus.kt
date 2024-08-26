package ru.hollowhorizon.hc.common.events

import ru.hollowhorizon.hc.common.coroutines.onMainThreadSync
import ru.hollowhorizon.hc.common.coroutines.scopeSync
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass

object EventBus {
    val listeners = ConcurrentHashMap<KClass<out Event>, MutableList<EventListener<out Event>>>()

    inline fun <reified T : Event> register(listener: EventListener<T>) {
        val list = listeners.getOrPut(T::class) { mutableListOf() }
        list.add(listener)
        list.sortBy { it.priority }
    }

    fun registerNoInline(type: Class<Event>, listener: EventListener<Event>) {
        val list = listeners.getOrPut(type.kotlin) { mutableListOf() }
        list.add(listener)
        list.sortBy { it.priority }
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

suspend inline fun <reified T : Event> awaitEvent(crossinline isValidCondition: (T) -> Boolean = { true }): T {
    var listener: EventListener<T>? = null

    val result: T = suspendCoroutine { continuation ->

        listener = EventListener { event ->
            scopeSync {
                onMainThreadSync {
                    if (isValidCondition(event)) continuation.resume(event)
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