package ru.hollowhorizon.hc.common.scripting.kotlin

import ru.hollowhorizon.hc.common.scripting.ScriptingCompiler
import kotlin.script.experimental.annotations.KotlinScript

@KotlinScript(
    compilationConfiguration = HollowScriptConfiguration::class
)
abstract class Script


fun main() {
    val text = """
            import ru.hollowhorizon.hc.HollowCore
            
            HollowCore.LOGGER.info("HollowCore Scripting Engine loaded!")
        """.trimIndent()

    ScriptingCompiler.compileText<Script>(text)
}