package ru.hollowhorizon.hc.client.imgui

import kotlin.math.abs
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

val PROPERTIES = HashMap<String, Any?>()

fun <T> Graphics.remember(value: () -> T): ReadWriteProperty<Any?, T> {
    return object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return PROPERTIES.computeIfAbsent(property.name) { value() } as T
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            PROPERTIES[property.name] = value
        }
    }
}

fun Graphics.animatable(value: () -> Float): ReadWriteProperty<Any?, Float> {
    return object : ReadWriteProperty<Any?, Float> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): Float {
            val old = PROPERTIES.computeIfAbsent(property.name + "_old") { value() } as Float
            var new = PROPERTIES.computeIfAbsent(property.name) { value() } as Float
            new = transition(new, old, 0.2f, 0.001f)
            PROPERTIES[property.name] = new
            return new
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float) {
            PROPERTIES[property.name + "_old"] = value
        }
    }
}

fun transition(current: Float, target: Float, velocity: Float, epsilon: Float = 0.1f): Float {
    var mCurrent = current
    val delta = target - current

    if (abs(delta) < epsilon) mCurrent = target

    mCurrent += delta * velocity
    return mCurrent
}