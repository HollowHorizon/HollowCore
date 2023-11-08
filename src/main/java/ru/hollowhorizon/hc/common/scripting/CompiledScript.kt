package ru.hollowhorizon.hc.common.scripting

import kotlinx.coroutines.runBlocking
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
    val saveFile: File?,
) {
    var errors: List<String>? = null

    init {
        if (saveFile != null && script != null) (script as KJvmCompiledScript).saveScriptToJar(saveFile, hash)
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
        return runBlocking { evaluator(script, evalConfig) }
    }
}