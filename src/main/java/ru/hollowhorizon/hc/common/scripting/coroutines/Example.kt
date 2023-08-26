package ru.hollowhorizon.hc.common.scripting.coroutines

import kotlinx.coroutines.*
import ru.hollowhorizon.hc.common.scripting.coroutines.PersistingWrapper.wrapper
import java.util.*

fun main() {
    val res = runBlocking {
        wrapper {
            val t1 = async(this.coroutineContext) {
                delay(1000)
                println("here")
                "10"
            }
            val t2 = async(this.coroutineContext) {
                delay(2000)
                println("here2")
                "20"
            }
            val res = t1.await() + t2.await()
            println(res)
            persist()
            res
        }
    }
    println(res)
}