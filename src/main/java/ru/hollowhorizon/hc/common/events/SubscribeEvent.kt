package ru.hollowhorizon.hc.common.events

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class SubscribeEvent(val priority: Int = 0, val clientSideOnly: Boolean = false)
