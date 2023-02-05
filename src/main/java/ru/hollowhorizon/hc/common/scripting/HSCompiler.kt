package ru.hollowhorizon.hc.common.scripting

import kotlinx.coroutines.runBlocking
import net.minecraftforge.fml.loading.FMLPaths
import java.io.*
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.FileScriptSource
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate


class HSCompiler private constructor() {
    fun toReadablePath(file: File): String {
        return file.path.substringAfter(FMLPaths.GAMEDIR.get().resolve("hollowscript").toFile().path+"\\").replace("\\", "/")
    }

    companion object {
        @JvmField
        val COMPILER = HSCompiler()

        @JvmStatic
        fun init() {
            runBlocking {
                COMPILER.compile<HollowScript>("""
                    import ru.hollowhorizon.hc.HollowCore
                    
                    HollowCore.LOGGER.info("Scripting Engine initialized!")
                """.trimIndent()).execute()
            }
        }
    }

    inline fun <reified T : Any> compile(code: String): CompiledHollowScript {
        val hostConfiguration = defaultJvmScriptingHostConfiguration

        val compilationConfiguration = createJvmCompilationConfigurationFromTemplate<T>()

        val compiled = runBlocking {

            val compiler = JvmScriptCompiler(hostConfiguration)

            compiler(code.toScriptSource(), compilationConfiguration)
                .valueOrThrow() as KJvmCompiledScript
        }
        return CompiledHollowScript("code", code.hashCode(), compiled)
    }

    inline fun <reified T : Any> compile(
        cacheDir: File = File("scripts"),
        script: File,
    ): CompiledHollowScript {
        val compiledFile = File(cacheDir, toReadablePath(script).substringBeforeLast(".")+".hollow")

        val hash = script.readText().hashCode()

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
                script.name, hash,
                compiler(FileScriptSource(script), compilationConfiguration)
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