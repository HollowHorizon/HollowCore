package ru.hollowhorizon.hc.common.scripting

import ru.hollowhorizon.hc.client.utils.isProduction_
import ru.hollowhorizon.hc.common.scripting.kotlin.HollowScript
import java.io.File
import kotlin.time.measureTime

fun main() {
    isProduction_ = { true }
    ScriptingCompiler.compileText<HollowScript>("fun main() {}")

    val time = measureTime {
        val script = ScriptingCompiler.compileFile<HollowScript>(
            File("test.kts")
        )

        println(script)

        //val result = script.execute()
        //val instance = result.valueOrThrow().returnValue.scriptInstance!!
        //instance::class.java.declaredMethods.first().invoke(instance)
    }

    println("Время компиляции и выполнения: $time")
}

