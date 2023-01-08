package ru.hollowhorizon.hc.common.scripting

import kotlinx.coroutines.runBlocking
import org.jetbrains.annotations.NotNull
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript

data class CompiledHollowScript(val scriptName: String, val hash: Int, val script: KJvmCompiledScript) {
    fun execute(body: ScriptEvaluationConfiguration.Builder.() -> Unit = {}): ResultWithDiagnostics<EvaluationResult> {
        val evalConfig = ScriptEvaluationConfiguration {
            body()
        }
        val evaluator = BasicJvmScriptEvaluator()
        return runBlocking { evaluator(script, evalConfig) }
    }
}