package ru.hollowhorizon.hc.common.scripting

import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.scripting.*
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
    val scriptFile: File?,
) {
    var errors: List<String>? = null

    fun save(file: File) {
        if (script == null) return
        (script as KJvmCompiledScript).saveScriptToJar(file, hash)
    }

    suspend fun execute(body: ScriptEvaluationConfiguration.Builder.() -> Unit = {}): ResultWithDiagnostics<EvaluationResult> {
        if (script == null) {
            return ResultWithDiagnostics.Failure(
                arrayListOf(
                    ScriptDiagnostic(-1, "Script not compiled!", ScriptDiagnostic.Severity.FATAL)
                )
            )
        }

        val evalConfig = ScriptEvaluationConfiguration { body() }
        val evaluator = BasicJvmScriptEvaluator()
        val result = evaluator(script, evalConfig)


        if (result is ResultWithDiagnostics.Success) ScriptStartedEvent(scriptFile)
        else if (result is ResultWithDiagnostics.Failure) {
            val errors = result.reports.map {
                ScriptError(
                    Severity.entries[it.severity.ordinal],
                    it.message,
                    it.sourcePath ?: "",
                    it.location?.start?.line ?: 0,
                    it.location?.start?.col ?: 0,
                    it.exception
                )
            }

            ScriptErrorEvent(scriptFile, ErrorType.RUNTIME_ERROR, errors).post()
        }

        return result
    }
}