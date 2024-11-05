package ru.hollowhorizon.hc.common.events

import ru.hollowhorizon.hc.common.coroutines.onMainThreadSync
import ru.hollowhorizon.hc.common.coroutines.scopeSync
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.reflect.KClass


object EventBus {
    val listeners = HashMap<KClass<out Event>, MutableList<EventListener<out Event>>>()

    inline fun <reified T : Event> register(listener: EventListener<T>) {
        val list = listeners.getOrPut(T::class) { CopyOnWriteArrayList() }
        list.add(listener)
        list.sortBy { it.priority }
    }

    fun registerNoInline(type: Class<Event>, listener: EventListener<Event>) {
        val list = listeners.getOrPut(type.kotlin) { CopyOnWriteArrayList() }
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

fun main() {
    class SEvent : Event

    var listener: EventListener<SEvent>? = null
    listener = EventListener {
        println("Hello world")
        EventBus.unregister(listener!!)
    }
    EventBus.register(listener)

    SEvent().post()
    SEvent().post()
    SEvent().post()
    SEvent().post()
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