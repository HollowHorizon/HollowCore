package ru.hollowhorizon.hc.common.scripting

import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

object ScriptingCompiler {
    inline fun <reified T : Any> compileText(code: String): CompiledScript {
        val hostConfiguration = defaultJvmScriptingHostConfiguration

        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<T>()

        val compiled = runBlocking {

            val compiler = JvmScriptCompiler(hostConfiguration)

            compiler(code.toScriptSource(), compilationConfiguration)
                .valueOrThrow() as KJvmCompiledScript
        }
        return CompiledScript("code.kts", code.hashCode(), compiled)
    }

    inline fun <reified T : Any> compileFile(script: File): CompiledScript {
        val hostConfiguration = defaultJvmScriptingHostConfiguration

        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<T>()

        return runBlocking {

            val compiler = JvmScriptCompiler(hostConfiguration)

            CompiledScript(
                script.name, script.readText().hashCode(),
                compiler(FileScriptSource(script), compilationConfiguration)
                    .valueOrThrow() as KJvmCompiledScript
            )
        }
    }
}