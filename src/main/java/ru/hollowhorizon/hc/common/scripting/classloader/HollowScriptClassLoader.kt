package ru.hollowhorizon.hc.common.scripting.classloader

import ru.hollowhorizon.hc.HollowCore
import java.io.File
import java.net.URLClassLoader

typealias ClassProvider = (name: String) -> Class<*>?

class HollowScriptClassLoader(
    val classProvider: ClassProvider,
    parent: ClassLoader,
    dependenciesFiles: Set<File>
) : URLClassLoader(
    dependenciesFiles.map { it.toURI().toURL() }.toTypedArray(),
    parent
) {
    override fun findClass(name: String): Class<*>? {
        return findClass(name, true)
    }

    fun findClass(name: String, checkGlobal: Boolean): Class<*>? {
        var clazz: Class<*>? = null

        HollowCore.LOGGER.info(name)

        if(checkGlobal) {
            clazz = classProvider(name)
        }

        if(clazz == null) {
            clazz = runCatching { super.findClass(name) }.getOrNull()
        }

        return clazz
    }
}