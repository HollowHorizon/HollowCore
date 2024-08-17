package ru.hollowhorizon.hc.common.events

import java.lang.invoke.LambdaMetafactory
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.lang.reflect.Method
import java.util.function.Consumer

fun interface EventListener<T : Event> {
    val priority: Int get() = 0

    fun onEvent(event: T)
}

@Suppress("UNCHECKED_CAST")
fun MethodHandles.Lookup.createEventListener(
    method: Method,
    target: Any
): EventListener<Event> {
    try {
        val methodHandle = unreflect(method)
        val callSite = LambdaMetafactory.metafactory(
            this,
            "accept",
            MethodType.methodType(Consumer::class.java, target.javaClass),
            MethodType.methodType(Void.TYPE, Any::class.java),
            methodHandle,
            MethodType.methodType(Void.TYPE, method.parameterTypes[0])
        )

        val priority = method.getAnnotation(SubscribeEvent::class.java).priority
        val eventHandle = callSite.target.bindTo(target).invokeWithArguments() as Consumer<Event>
        return object : EventListener<Event> {
            override val priority = priority

            override fun onEvent(event: Event) {
                eventHandle.accept(event)
            }

        }
    } catch (t: Throwable) {
        throw IllegalStateException("Error while registering $method", t)
    }
}

@Suppress("UNCHECKED_CAST")
fun MethodHandles.Lookup.createStaticEventListener(method: Method): EventListener<Event> {
    try {
        val methodHandle = unreflect(method)
        val callSite = LambdaMetafactory.metafactory(
            this,
            "accept",
            MethodType.methodType(Consumer::class.java),
            MethodType.methodType(Void.TYPE, Any::class.java),
            methodHandle,
            MethodType.methodType(Void.TYPE, method.parameterTypes[0])
        )

        val priority = method.getAnnotation(SubscribeEvent::class.java).priority
        val eventHandle = callSite.target.invoke() as Consumer<Event>
        return object : EventListener<Event> {
            override val priority = priority

            override fun onEvent(event: Event) {
                eventHandle.accept(event)
            }

        }
    } catch (t: Throwable) {
        throw IllegalStateException("Error while registering $method", t)
    }
}
