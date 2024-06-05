package ru.hollowhorizon.hc.common.events

import kotlin.reflect.KClass

object EventBus {
    val listeners = hashMapOf<KClass<out Event>, MutableList<EventListener<out Event>>>()
    val cachedListeners = hashMapOf<KClass<out Event>, MutableList<EventListener<out Event>>>()

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

        for ((key, eventListeners) in listeners) {
            if (key.isInstance(event)) {
                for (listener in eventListeners) {
                    if (cancelable?.isCanceled == true) return
                    (listener as EventListener<T>).onEvent(event)
                }
            }
        }
    }
}