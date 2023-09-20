package ru.hollowhorizon.hc.common.scripting.states

import java.util.*

class ScriptExecutor {
    val data = Stack<() -> Any?>()
    operator fun invoke() {
        while (data.size > 0) data.pop()?.invoke()
    }
}

fun <T> ScriptExecutor.saveable(function: () -> T) {
    data.push(function)
}

fun ScriptExecutor.script() = saveable {
    println(1)
    delay(1f)
    other()
    println(2)
    delay(1f)
    println(3)
}

fun ScriptExecutor.other() = saveable {
    println(4)
    delay(1f)
    println(5)
}

fun ScriptExecutor.delay(time: Float) = saveable {
    Thread.sleep((time * 1000).toLong())
}

fun main() {
    val saveable = ScriptExecutor().apply { script() }

    saveable()
}
