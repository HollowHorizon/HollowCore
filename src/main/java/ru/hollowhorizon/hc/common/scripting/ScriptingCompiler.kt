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

import imgui.ImGui
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.scripting.*
import ru.hollowhorizon.hc.common.scripting.kotlin.AbstractHollowScriptHost
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.host.createCompilationConfigurationFromTemplate
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.util.isError
import kotlin.script.experimental.jvmhost.BasicJvmScriptJarGenerator
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.loadScriptFromJar
import kotlin.script.experimental.util.PropertiesCollection

fun <R> ResultWithDiagnostics<R>.orException(): R = valueOr {
    throw IllegalStateException(
        it.errors().joinToString("\n"),
        it.reports.find { it.exception != null }?.exception
    )
}

fun ResultWithDiagnostics.Failure.errors(): List<String> = reports.map { diagnostic ->
    buildString {
        if (diagnostic.severity >= ScriptDiagnostic.Severity.WARNING) {
            append(diagnostic.message)

            if (diagnostic.sourcePath != null || diagnostic.location != null) {
                append(" at [")
                diagnostic.sourcePath?.let { append(it.substringAfterLast(File.separatorChar)) }
                diagnostic.location?.let { path ->
                    append(':')
                    append(path.start.line)
                    append(':')
                    append(path.start.col)
                }
                append("]")
            }
            if (diagnostic.exception != null) {
                append(": ")
                append(diagnostic.exception)
                ByteArrayOutputStream().use { os ->
                    val ps = PrintStream(os)
                    diagnostic.exception?.printStackTrace(ps)
                    ps.flush()
                    append("\n")
                    append(os.toString())
                }
            }
        }
    }
}.filter { it.isNotEmpty() }

object ScriptingCompiler {

    suspend inline fun <reified T : Any> compileText(text: String): CompiledScript {
        val hostConfiguration = AbstractHollowScriptHost()

        val compilationConfiguration = createCompilationConfigurationFromTemplate(
            KotlinType(T::class),
            hostConfiguration,
            HollowCore::class
        ) {}


        val compiler = JvmScriptCompiler(hostConfiguration)
        val result = compiler(StringScriptSource(text), compilationConfiguration)

        result.reports.map {
            ScriptError(
                Severity.entries[it.severity.ordinal],
                it.message,
                it.sourcePath ?: "",
                it.location?.start?.line ?: 0,
                it.location?.start?.col ?: 0,
                it.exception
            )
        }.forEach {
            HollowCore.LOGGER.error(it.format())
        }

        return CompiledScript(
            "script.kts", "",
            (result.valueOrNull() as KJvmCompiledScript).obfuscate("script.kts"), File("").resolve("script.kts")
        ).apply {
            if (result.isError()) {
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

                val event = ScriptErrorEvent(null, ErrorType.COMPILATION_ERROR, errors)
                event.post()
                if (!event.isCanceled) {
                    this.errors =
                        if (result.isError()) (result as ResultWithDiagnostics.Failure).errors() else null
                }
            }

        }

    }

    suspend inline fun <reified T : Any> compileFile(script: File): CompiledScript {
        val hostConfiguration = AbstractHollowScriptHost()

        val compilationConfiguration = createCompilationConfigurationFromTemplate(
            KotlinType(T::class),
            hostConfiguration,
            HollowCore::class
        ) {}

        val compiledJar = script.absoluteFile.parentFile.resolve(script.name + ".jar")
        val hashcode = script.readText().hashCode().toString()

        if (compiledJar.exists()) {
            return CompiledScript(
                script.name, hashcode,
                compiledJar.loadScriptFromJar(), script
            )
        }

        val compiler = JvmScriptCompiler(hostConfiguration)
        val compiled = compiler(FileScriptSource(script), compilationConfiguration)

        return CompiledScript(
            script.name, hashcode,
            (compiled.valueOrNull() as KJvmCompiledScript).obfuscate(script.name), script
        ).apply {
            if (compiled.isError()) {
                val errors = compiled.reports.map {
                    ScriptError(
                        Severity.entries[it.severity.ordinal],
                        it.message,
                        it.sourcePath ?: "",
                        it.location?.start?.line ?: 0,
                        it.location?.start?.col ?: 0,
                        it.exception
                    )
                }

                val event = ScriptErrorEvent(script, ErrorType.COMPILATION_ERROR, errors)
                event.post()
                if (!event.isCanceled) {
                    this.errors =
                        if (compiled.isError()) (compiled as ResultWithDiagnostics.Failure).errors() else null
                }
            } else {
                save(compiledJar)
                ScriptCompiledEvent(script).post()
            }


        }
    }

    suspend fun KJvmCompiledScript.saveScriptToJar(outputJar: File) {
        BasicJvmScriptJarGenerator(outputJar)(this, ScriptEvaluationConfiguration())
    }
}
