package ru.hollowhorizon.hc.common.events.scripting

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import ru.hollowhorizon.hc.common.events.Cancelable
import ru.hollowhorizon.hc.common.events.Event
import java.io.File

open class ScriptEvent(val file: File?) : Event

@OptIn(ExperimentalCompilerApi::class)
class ScriptingCompilerPluginEvent(private val registrar: (CompilerPluginRegistrar) -> Unit) : Event {
    fun addExtension(extension: CompilerPluginRegistrar) {
        registrar(extension)
    }
}

class ScriptErrorEvent(file: File?, val type: ErrorType, val error: List<ScriptError>) : ScriptEvent(file), Cancelable {
    override var isCanceled = false
}

class ScriptCompiledEvent(file: File) : ScriptEvent(file)
class ScriptStartedEvent(file: File?) : ScriptEvent(file)

data class ScriptError(
    val severity: Severity,
    val message: String,
    val source: String,
    val line: Int,
    val column: Int,
    val exception: Throwable?,
) {
    fun format() = "$message at $source:$line:$column"
}

enum class Severity {
    DEBUG, INFO, WARNING, ERROR, FATAL
}

enum class ErrorType {
    COMPILATION_ERROR, RUNTIME_ERROR
}