package ru.hollowhorizon.hc.common.scripting.sandbox.context

import groovy.lang.Script

interface IScriptContext {
    val bindings: Map<String, Object>
    val baseClass: Class<*>

    fun onError(message: String, throwable: Throwable?)
    fun onError(throwable: Throwable) = onError("Error!", throwable)
    fun onError(message: String) = onError(message, null)
    fun onRunScriptPre(script: Script)
    fun onRunScriptPost(script: Script)
}