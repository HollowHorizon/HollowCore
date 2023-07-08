package ru.hollowhorizon.hc.common.scripting.kotlin

import java.io.File
import java.io.InputStream
import java.security.ProtectionDomain
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarInputStream
import kotlin.reflect.KClass
import kotlin.script.experimental.api.*
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.impl.KJvmCompiledScript
import kotlin.script.experimental.jvm.impl.createScriptFromClassLoader
import kotlin.script.experimental.jvm.jvm

fun File.loadScriptHashCode() = inputStream().use { istream ->
    JarInputStream(istream).use {
        it.manifest.mainAttributes.getValue("Script-Hashcode")
            ?: throw IllegalArgumentException("No Script-Hashcode manifest attribute")
    }
}


fun File.loadScriptFromJar(): CompiledScript {
    val className = inputStream().use { istream ->
        JarInputStream(istream).use {
            it.manifest.mainAttributes.getValue("Main-Class")
                ?: throw IllegalArgumentException("No Main-Class manifest attribute")
        }
    }
    return KJvmCompiledScriptFromJar(className, this)
}


internal class KJvmCompiledScriptFromJar(private val scriptClassFQName: String, private val file: File) :
    CompiledScript {
    private var loadedScript: KJvmCompiledScript? = null

    private fun getScriptOrFail(): KJvmCompiledScript =
        loadedScript ?: throw RuntimeException("Compiled script is not loaded yet")

    override suspend fun getClass(scriptEvaluationConfiguration: ScriptEvaluationConfiguration?): ResultWithDiagnostics<KClass<*>> {
        if (loadedScript == null) {
            val actualEvalConfig = scriptEvaluationConfiguration ?: ScriptEvaluationConfiguration()
            val baseClassLoader = actualEvalConfig[ScriptEvaluationConfiguration.jvm.baseClassLoader]
                ?: Thread.currentThread().contextClassLoader
            val classLoader = createScriptMemoryClassLoader(baseClassLoader)
            loadedScript = createScriptFromClassLoader(scriptClassFQName, classLoader)
        }
        return getScriptOrFail().getClass(scriptEvaluationConfiguration)
    }

    override val compilationConfiguration: ScriptCompilationConfiguration
        get() = getScriptOrFail().compilationConfiguration

    override val sourceLocationId: String?
        get() = loadedScript?.sourceLocationId

    override val otherScripts: List<CompiledScript>
        get() = getScriptOrFail().otherScripts

    override val resultField: Pair<String, KotlinType>?
        get() = getScriptOrFail().resultField

    private fun createScriptMemoryClassLoader(parent: ClassLoader?): ClassLoader {
        val file = JarFile(file)
        val entries = file.entries().asSequence().associate { it.name to file.getInputStream(it).readBytes() }
        return MemoryClassLoader(entries, parent)
    }

    private fun JarInputStream.readEntries(): Map<String, ByteArray> {
        return generateSequence(::getNextJarEntry)
            .associate { Pair(it.name, readBytes()) }
    }

    private fun Enumeration<JarEntry>.nextElementOrNull() = if (this.hasMoreElements()) this.nextElement() else null
}

internal class MemoryClassLoader(private val resources: Map<String, ByteArray>, parent: ClassLoader?) :
    ClassLoader(parent) {
    override fun findClass(name: String): Class<*> {
        val resource = name.replace('.', '/') + ".class"

        return resources[resource]?.let { bytes ->
            val protectionDomain = ProtectionDomain(null, null)
            return defineClass(name, bytes, 0, bytes.size, protectionDomain)
        }
            ?: throw ClassNotFoundException(name)
    }

    override fun getResourceAsStream(name: String): InputStream? {
        return resources[name]?.inputStream()
    }
}

