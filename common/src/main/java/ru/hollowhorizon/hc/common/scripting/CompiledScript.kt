/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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