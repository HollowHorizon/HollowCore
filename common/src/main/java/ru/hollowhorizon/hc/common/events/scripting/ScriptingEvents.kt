package ru.hollowhorizon.hc.common.events.scripting

import ru.hollowhorizon.hc.common.events.Cancelable
import ru.hollowhorizon.hc.common.events.Event
import java.io.File

open class ScriptEvent(val file: File) : Event

class ScriptErrorEvent(file: File, val type: ErrorType, val error: List<ScriptError>) : ScriptEvent(file), Cancelable {
    override var isCanceled = false

}

class ScriptCompiledEvent(file: File) : ScriptEvent(file)
class ScriptStartedEvent(file: File) : ScriptEvent(file)

class ScriptError(
    val severity: Severity,
    val message: String,
    val source: String,
    val line: Int,
    val column: Int,
    val exception: Throwable?,
)

enum class Severity {
    DEBUG, INFO, WARNING, ERROR, FATAL
}

enum class ErrorType {
    COMPILATION_ERROR, RUNTIME_ERROR
}