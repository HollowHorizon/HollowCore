package ru.hollowhorizon.hc.common.scripting

import kotlinx.coroutines.runBlocking
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.scripting.ScriptingCompiler.saveScriptToJar
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.impl.*
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

    fun KJvmCompiledScript.saveScriptToJar(outputJar: File) {
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
                    putValue("Created-By", "JetBrains Kotlin")
                    putValue("Main-Class", scriptClassFQName)
                }
            }

            // Create a new JarOutputStream for writing
            JarOutputStream(fileStream, manifest).use { jar ->
                // Write sanitized compiled script metadata
                jar.putNextEntry(JarEntry(scriptMetadataPath(scriptClassFQName)))
                jar.write(copyWithoutModule().toBytes())
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