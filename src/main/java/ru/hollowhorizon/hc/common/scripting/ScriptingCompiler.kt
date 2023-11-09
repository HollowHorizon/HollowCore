package ru.hollowhorizon.hc.common.scripting

import kotlinx.coroutines.runBlocking
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.scripting.kotlin.AbstractHollowScriptHost
import ru.hollowhorizon.hc.common.scripting.kotlin.HollowScript
import ru.hollowhorizon.hc.common.scripting.kotlin.loadScriptFromJar
import ru.hollowhorizon.hc.common.scripting.kotlin.loadScriptHashCode
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.script.experimental.api.*
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.createCompilationConfigurationFromTemplate
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.impl.*
import kotlin.script.experimental.jvm.util.isError
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate
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
            append('\n')
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
    inline fun <reified T : Any> compileText(code: String): CompiledScript {
        val hostConfiguration = defaultJvmScriptingHostConfiguration

        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<T>()

        val compiled = runBlocking {

            val compiler = JvmScriptCompiler(hostConfiguration)

            compiler(code.toScriptSource(), compilationConfiguration)
        }
        return CompiledScript("code.kts", code.hashCode().toString(), compiled.valueOrNull(), null).apply {
            errors = if(compiled.isError()) (compiled as ResultWithDiagnostics.Failure).errors() else null
        }
    }

    inline fun <reified T : Any> compileFile(script: File): CompiledScript {
        val hostConfiguration = AbstractHollowScriptHost()

        val compilationConfiguration = createCompilationConfigurationFromTemplate(
            KotlinType(T::class),
            hostConfiguration,
            HollowCore::class
        ) {}

        return runBlocking {
            val compiledJar = script.parentFile.resolve(script.name + ".jar")
            val hashcode = script.readText().hashCode().toString()

            if (compiledJar.exists() && compiledJar.loadScriptHashCode() == hashcode) {
                return@runBlocking CompiledScript(
                    script.name, hashcode,
                    compiledJar.loadScriptFromJar(), null
                )
            }

            val compiler = JvmScriptCompiler(hostConfiguration)
            val compiled = compiler(FileScriptSource(script), compilationConfiguration)

            return@runBlocking CompiledScript(
                script.name, hashcode,
                compiled.valueOrNull(), compiledJar
            ).apply {
                errors = if(compiled.isError()) (compiled as ResultWithDiagnostics.Failure).errors() else null
            }
        }
    }

    fun shouldRecompile(script: File): Boolean {
        val compiledJar = script.parentFile.resolve(script.name + ".jar")
        return compiledJar.exists() && compiledJar.loadScriptHashCode() != script.readText().hashCode().toString()
    }

    fun KJvmCompiledScript.saveScriptToJar(outputJar: File, hash: String) {
        HollowCore.LOGGER.info("saving script jar to: {}", outputJar.absolutePath)
        // Get the compiled module, which contains the output files
        val module = getCompiledModule().let { module ->
            // Ensure the module is of the correct type
            // (other types may be returned if the script is cached, for example, which is undesired)
            module as? KJvmCompiledModuleInMemory ?: throw IllegalArgumentException("Unsupported module type $module")
        }
        FileOutputStream(outputJar).use { fileStream ->
            // The compiled script jar manifest
            val manifest = Manifest().apply {
                mainAttributes.apply {
                    putValue("Manifest-Version", "1.0")
                    putValue("Created-By", "HollowCore ScriptingEngine")
                    putValue("Script-Hashcode", hash)
                    putValue("Main-Class", scriptClassFQName)
                }
            }

            // Create a new JarOutputStream for writing
            JarOutputStream(fileStream, manifest).use { jar ->
                // Write sanitized compiled script metadata
                jar.putNextEntry(JarEntry(scriptMetadataPath(scriptClassFQName)))
                jar.write(copyWithoutModule().apply(::shrinkSerializableScriptData).toBytes())
                jar.closeEntry()

                // Write each output file
                module.compilerOutputFiles.forEach { (path, bytes) ->
                    jar.putNextEntry(JarEntry(path))
                    jar.write(bytes)
                    jar.closeEntry()
                }

                jar.finish()
                jar.flush()
            }
            fileStream.flush()
        }
    }
}


private fun shrinkSerializableScriptData(compiledScript: KJvmCompiledScript) {
    (compiledScript.compilationConfiguration.entries() as? MutableSet<Map.Entry<PropertiesCollection.Key<*>, Any?>>)
        ?.removeIf { it.key == ScriptCompilationConfiguration.dependencies || it.key == ScriptCompilationConfiguration.defaultImports }
}

fun main() {
    val command = "println(\"Hello World!\")"

    val result = ScriptingCompiler.compileText<HollowScript>(command).execute()

    result.reports.map { it.render() }.forEach(::println)

    println(result.valueOrThrow().returnValue)
}