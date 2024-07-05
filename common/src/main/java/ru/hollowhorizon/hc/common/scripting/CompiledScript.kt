package ru.hollowhorizon.hc.common.scripting

import kotlinx.coroutines.runBlocking
import ru.hollowhorizon.hc.common.events.scripting.ScriptStartedEvent
import ru.hollowhorizon.hc.common.scripting.ScriptingCompiler.saveScriptToJar
import java.io.File
import kotlin.script.experimental.api.*
import kotlin.script.experimental.api.CompiledScript
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript

data class CompiledScript(
    val scriptName: String,
    val hash: String,
    val script: CompiledScript?,
    val scriptFile: File,
) {
    var errors: List<String>? = null

    fun save(file: File) {
        if (script == null) return
        (script as KJvmCompiledScript).saveScriptToJar(file, hash)
    }

    fun execute(body: ScriptEvaluationConfiguration.Builder.() -> Unit = {}): ResultWithDiagnostics<EvaluationResult> {
        if (script == null) {
            return ResultWithDiagnostics.Failure(
                arrayListOf(
                    ScriptDiagnostic(-1, "Script not compiled!", ScriptDiagnostic.Severity.FATAL)
                )
            )
        }

        val evalConfig = ScriptEvaluationConfiguration { body() }
        val evaluator = BasicJvmScriptEvaluator()
        val result = runBlocking {
            evaluator(script, evalConfig)
        }


        if (result is ResultWithDiagnostics.Success) ScriptStartedEvent(scriptFile)


        return result
    }
}