package ru.hollowhorizon.hc.common.capabilities

import it.unimi.dsi.fastutil.objects.Object2ObjectFunction
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST")
open class CapabilityProperty<T : CapabilityInstance, V : Any?>(val default: V) : ReadWriteProperty<T, V> {

    val method = Object2ObjectFunction<String, V> { default }

    override fun getValue(thisRef: T, property: KProperty<*>): V {
        return thisRef.properties.computeIfAbsent(property.name, method) as V
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
        thisRef.properties[property.name] = value
        thisRef.sync()
    }
}