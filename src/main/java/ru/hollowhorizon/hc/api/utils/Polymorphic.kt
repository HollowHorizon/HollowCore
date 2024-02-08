package ru.hollowhorizon.hc.api.utils

import kotlin.reflect.KClass

annotation class Polymorphic(val baseClass: KClass<*>)