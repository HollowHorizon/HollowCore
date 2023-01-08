package ru.hollowhorizon.hc.common.scripting.remapper

import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.config.javaSourceRoots
import org.jetbrains.kotlin.cli.jvm.config.jvmClasspathRoots
import org.jetbrains.kotlin.com.intellij.lang.jvm.JvmModifier
import org.jetbrains.kotlin.com.intellij.openapi.vfs.StandardFileSystems
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.com.intellij.psi.PsiClass
import org.jetbrains.kotlin.com.intellij.psi.PsiJavaFile
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.config.JVMConfigurationKeys

class ShortNameIndex(private val environment: KotlinCoreEnvironment) {
    private val psiManager = PsiManager.getInstance(environment.project)

    private val entries: Map<String, ShortNameEntry> = mutableMapOf<String, ShortNameEntry>().apply {
        val localFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
        val jarFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.JAR_PROTOCOL)
        val jrtFileSystem = VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.JRT_PROTOCOL)

        val classpathRoots = environment.configuration.jvmClasspathRoots.mapNotNull { file ->
            if (file.isFile) {
                jarFileSystem.findFileByPath("${file.absolutePath}!/")
            } else {
                localFileSystem.findFileByPath(file.absolutePath)
            }
        }

        val jdkHome = environment.configuration[JVMConfigurationKeys.JDK_HOME]
        val allModuleRoots = jrtFileSystem.findFileByPath("$jdkHome!/modules")?.children ?: emptyArray()
        val javaModuleRoots = allModuleRoots.filter { it.name.startsWith("java.") }

        val sourcesRoots = environment.configuration.javaSourceRoots.mapNotNull { localFileSystem.findFileByPath(it) }

        fun index(file: VirtualFile, pkgPrefix: String) {
            if (file.isDirectory) {
                val pkg = "$pkgPrefix${file.name}."
                file.children.forEach { index(it, pkg) }
            } else if (file.extension == "class") {
                val fileName = file.nameWithoutExtension
                val shortName = if ('$' in fileName) {
                    val innerName = fileName.substringAfterLast('$')
                    if (!innerName.first().isJavaIdentifierStart()) {
                        return
                    }
                    innerName
                } else {
                    fileName
                }
                getOrPut(shortName, ::ShortNameEntry).files.add(file)
            } else if (file.extension == "java") {
                val psi = psiManager.findFile(file) as? PsiJavaFile ?: return
                psi.classes.flatMap { listOf(it) + it.allInnerClasses }.forEach { psiClass ->
                    getOrPut(psiClass.name ?: return@forEach, ::ShortNameEntry).files.add(file)
                }
            }
        }

        (classpathRoots + javaModuleRoots + sourcesRoots).forEach { root ->
            root.children.forEach { index(it, "") }
        }
    }

    operator fun get(shortName: String): Set<PsiClass> {
        val entry = entries[shortName] ?: return emptySet()
        return entry.resolve(psiManager, shortName)
    }

    private class ShortNameEntry {
        var files = mutableListOf<VirtualFile>()
        private var classes: Set<PsiClass>? = null

        fun resolve(psiManager: PsiManager, shortName: String): Set<PsiClass> {
            return classes ?: resolveClasses(psiManager, shortName)
        }

        private fun resolveClasses(psiManager: PsiManager, shortName: String): Set<PsiClass> {
            val result = files.flatMap { file ->
                if (file.extension == "java" && file.nameWithoutExtension != shortName) {
                    val psi = psiManager.findFile(file) as? PsiJavaFile ?: return@flatMap emptyList()
                    psi.classes.flatMap { sequenceOf(it) + it.allInnerClasses.asIterable() }
                        .filter { it.qualifiedName?.endsWith(shortName) == true }
                } else if ('$' in file.name) {
                    val className = file.nameWithoutExtension.replace('$', '.')
                    val outerName = className.substringBefore(".")
                    val outerFile = file.parent.findChild("$outerName.class") ?: return@flatMap emptyList()
                    val outerPsi = psiManager.findFile(outerFile) as? PsiJavaFile ?: return@flatMap emptyList()
                    outerPsi.classes.flatMap { it.allInnerClasses.asIterable() }
                        .filter { it.qualifiedName?.endsWith(className) == true }
                } else {
                    (psiManager.findFile(file) as? PsiJavaFile)?.classes?.asIterable() ?: emptyList()
                }
            }.filter { it.hasModifier(JvmModifier.PUBLIC) }.toSet()
            classes = result
            return result
        }
    }
}