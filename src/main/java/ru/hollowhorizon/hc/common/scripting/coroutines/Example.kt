package ru.hollowhorizon.hc.common.scripting.coroutines

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import ru.hollowhorizon.hc.common.scripting.coroutines.PersistingWrapper.wrapper
import kotlin.concurrent.thread

fun main() {
    runBlocking {
        wrapper {
            println("World")
            persistAndSave()
            println("!")

            val result = thread {
                println("Hello 1")
                Thread.sleep(500L)
                println("Hello 2")
            }
            result.start()
            delay(250L)
            println("World 2")
            persistAndSave()
            result.join()
        }
    }

}