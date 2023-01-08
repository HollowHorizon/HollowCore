package ru.hollowhorizon.hc.common.scripting

import kotlinx.coroutines.runBlocking
import java.io.*
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.StringScriptSource
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate


class HSCompiler {
    inline fun <reified T : Any> compile(name: String, script: InputStream): CompiledHollowScript {
        return compile<T>(name, script.bufferedReader().readText())
    }

    inline fun <reified T : Any> compile(name: String, script: String): CompiledHollowScript {
        val compiledFile = File("scripts/$name.hollow")
        val hash = script.hashCode()
        if (compiledFile.exists()) {
            val compiled = this.load(compiledFile)
            if (compiled.hash == hash) {
                return compiled
            }

            compiledFile.delete()
        }

        val hostConfiguration = defaultJvmScriptingHostConfiguration

        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<T>()
        val compiled = runBlocking {

            val compiler = JvmScriptCompiler(hostConfiguration)

            CompiledHollowScript(
                name, hash,
                compiler(StringScriptSource(script), compilationConfiguration)
                    .valueOrThrow() as KJvmCompiledScript
            )
        }

        this.save(compiledFile, compiled)
        return compiled
    }

    fun load(file: File): CompiledHollowScript {
        val stream = ObjectInputStream(FileInputStream(file))
        if (stream.readUTF() != "HOLLOW") {
            throw (IllegalArgumentException("Not a HollowScript file"))
        } else {
            val hash = stream.readInt()
            val scriptName = stream.readUTF()
            val script = stream.readObject() as KJvmCompiledScript
            return CompiledHollowScript(scriptName, hash, script)
        }
    }

    fun save(file: File, script: CompiledHollowScript) {
        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
            println("Created file ${file.absolutePath}")
        }
        val stream = ObjectOutputStream(FileOutputStream(file))
        stream.writeUTF("HOLLOW")
        stream.writeInt(script.hash)
        stream.writeUTF(script.scriptName)
        stream.writeObject(script.script)
    }
}
