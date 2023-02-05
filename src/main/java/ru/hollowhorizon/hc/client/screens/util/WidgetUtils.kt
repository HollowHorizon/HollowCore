package ru.hollowhorizon.hc.client.screens.util

fun <T> mutableStateOf(initialValue: T): MutableState<T> = MutableState(initialValue)

class MutableState<T>(initialValue: T) {
    var value: T = initialValue
        set(value) {
            field = value
            onChangeListeners.forEach { it(value) }
        }

    private val onChangeListeners = mutableListOf<(T) -> Unit>()

    fun addListener(listener: (T) -> Unit) {
        onChangeListeners.add(listener)
    }
}