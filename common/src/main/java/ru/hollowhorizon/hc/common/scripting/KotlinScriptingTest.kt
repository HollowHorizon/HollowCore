package ru.hollowhorizon.hc.common.scripting

import ru.hollowhorizon.hc.client.utils.isProduction_
import ru.hollowhorizon.hc.common.scripting.kotlin.HollowScript
import kotlin.script.experimental.api.valueOrThrow
import kotlin.time.measureTime

fun main() {
    isProduction_ = { false }

    ScriptingCompiler.compileText<HollowScript>(
        """
        fun main() {
            var x = 2
            var y = 3
            println("x = "+x)
            println("y = "+y)
            println("x + y = "+(x+y))
        }
    """.trimIndent()
    )

    val time = measureTime {
        val script = ScriptingCompiler.compileText<HollowScript>(
            """
        fun main() {
            var x = 2
            var y = 3
            println("x = "+x)
            println("y = "+y)
            println("x + y = "+(x+y))
        }
    """.trimIndent()
        )

        val result = script.execute()
        val instance = result.valueOrThrow().returnValue.scriptInstance!!
        instance::class.java.declaredMethods.first().invoke(instance)
    }

    println("Время компиляции и выполнения: $time")
}