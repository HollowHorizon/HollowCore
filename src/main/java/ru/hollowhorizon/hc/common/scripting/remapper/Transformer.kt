package ru.hollowhorizon.hc.common.scripting.remapper

import org.cadixdev.lorenz.MappingSet
import org.cadixdev.lorenz.io.MappingFormats
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot
import org.jetbrains.kotlin.cli.common.messages.MessageRenderer
import org.jetbrains.kotlin.cli.common.messages.PrintingMessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.config.JavaSourceRoot
import org.jetbrains.kotlin.cli.jvm.config.JvmClasspathRoot
import org.jetbrains.kotlin.com.intellij.codeInsight.CustomExceptionHandler
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.extensions.ExtensionPoint
import org.jetbrains.kotlin.com.intellij.openapi.extensions.Extensions
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.openapi.vfs.StandardFileSystems
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.com.intellij.openapi.vfs.local.CoreLocalFileSystem
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.com.intellij.psi.search.GlobalSearchScope
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.JVMConfigurationKeys
import org.jetbrains.kotlin.psi.KtFile
import java.io.File
import java.io.IOException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import java.util.*
import kotlin.io.path.absolutePathString

class Transformer(private val mappings: MappingSet) {
    var classpath: Array<String>? = null
    var remappedClasspath: Array<String>? = null
    var patternAnnotation: String? = null
    var manageImports = false

    @Throws(IOException::class)
    fun remap(sources: Map<String, String>): Map<String, Pair<String, List<Pair<Int, String>>>> =
        remap(sources, emptyMap())

    @Throws(IOException::class)
    fun remap(
        sources: Map<String, String>,
        processedSources: Map<String, String>,
    ): Map<String, Pair<String, List<Pair<Int, String>>>> {
        val tmpDir = Files.createTempDirectory("remap")
        val processedTmpDir = Files.createTempDirectory("remap-processed")
        val disposable = Disposer.newDisposable()
        try {
            for ((unitName, source) in sources) {
                val path = tmpDir.resolve(unitName)
                Files.createDirectories(path.parent)
                Files.write(path, source.toByteArray(StandardCharsets.UTF_8), StandardOpenOption.CREATE)

                val processedSource = processedSources[unitName] ?: source
                val processedPath = processedTmpDir.resolve(unitName)
                Files.createDirectories(processedPath.parent)
                Files.write(processedPath, processedSource.toByteArray(), StandardOpenOption.CREATE)
            }

            val config = CompilerConfiguration()
            config.put(CommonConfigurationKeys.MODULE_NAME, "main")
            config.add(CLIConfigurationKeys.CONTENT_ROOTS, JavaSourceRoot(tmpDir.toFile(), ""))
            config.add(CLIConfigurationKeys.CONTENT_ROOTS, KotlinSourceRoot(tmpDir.toAbsolutePath().toString(), false))
            config.addAll(CLIConfigurationKeys.CONTENT_ROOTS, classpath!!.map { JvmClasspathRoot(File(it)) })
            config.put(
                CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                PrintingMessageCollector(System.err, MessageRenderer.GRADLE_STYLE, true)
            )

            // Our PsiMapper only works with the PSI tree elements, not with the faster (but kotlin-specific) classes
            config.put(JVMConfigurationKeys.USE_PSI_CLASS_FILES_READING, true)

            var environment: KotlinCoreEnvironment? = null
            try {
                environment = KotlinCoreEnvironment.createForProduction(
                    disposable,
                    config,
                    EnvironmentConfigFiles.JVM_CONFIG_FILES
                )
            } catch (_: Exception) {
                //Sometimes it throws an exception, but it still works
            }

            val rootArea = Extensions.getRootArea()
            synchronized(rootArea) {
                if (!rootArea.hasExtensionPoint(CustomExceptionHandler.KEY)) {
                    rootArea.registerExtensionPoint(
                        CustomExceptionHandler.KEY.name,
                        CustomExceptionHandler::class.java.name,
                        ExtensionPoint.Kind.INTERFACE
                    )
                }
            }

            val project = environment!!.project as MockProject
            val psiManager = PsiManager.getInstance(project)
            val vfs =
                VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL) as CoreLocalFileSystem
            val virtualFiles = sources.mapValues { vfs.findFileByIoFile(tmpDir.resolve(it.key).toFile())!! }
            val psiFiles = virtualFiles.mapValues { psiManager.findFile(it.value)!! }
            val ktFiles = psiFiles.values.filterIsInstance<KtFile>()


            val analysis = TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
                environment.project,
                ktFiles,
                NoScopeRecordCliBindingTrace(),
                environment.configuration,
                { scope: GlobalSearchScope -> environment.createPackagePartProvider(scope) }
            )


            val remappedEnv = remappedClasspath?.let {
                setupRemappedProject(disposable, it, processedTmpDir)
            }

            val patterns = patternAnnotation?.let { annotationFQN ->
                val patterns = PsiPatterns(annotationFQN)
                val annotationName = annotationFQN.substring(annotationFQN.lastIndexOf('.') + 1)
                for ((unitName, source) in sources) {
                    if (!source.contains(annotationName)) continue
                    try {
                        val patternFile = vfs.findFileByIoFile(tmpDir.resolve(unitName).toFile())!!
                        val patternPsiFile = psiManager.findFile(patternFile)!!
                        patterns.read(patternPsiFile, processedSources[unitName]!!)
                    } catch (e: Exception) {
                        throw RuntimeException("Failed to read patterns from file \"$unitName\".", e)
                    }
                }
                patterns
            }

            val autoImports = if (manageImports && remappedEnv != null) {
                AutoImports(remappedEnv)
            } else {
                null
            }

            val results = HashMap<String, Pair<String, List<Pair<Int, String>>>>()
            for (name in sources.keys) {
                val file = vfs.findFileByIoFile(tmpDir.resolve(name).toFile())!!
                val psiFile = psiManager.findFile(file)!!

                var (text, errors) = try {
                    PsiMapper(mappings, remappedEnv?.project, psiFile, analysis.bindingContext, patterns).remapFile()
                } catch (e: Exception) {
                    throw RuntimeException("Failed to map file \"$name\".", e)
                }

                if (autoImports != null && "/* remap: no-manage-imports */" !in text) {
                    val processedText = processedSources[name] ?: text
                    text = autoImports.apply(psiFile, text, processedText)
                }

                results[name] = text to errors
            }
            return results
        } finally {
            Files.walk(tmpDir).sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
            Files.walk(processedTmpDir).sorted(Comparator.reverseOrder()).forEach { Files.delete(it) }
            Disposer.dispose(disposable)
        }
    }

    private fun setupRemappedProject(
        disposable: Disposable,
        classpath: Array<String>,
        sourceRoot: Path,
    ): KotlinCoreEnvironment {
        val config = CompilerConfiguration()
        config.put(CommonConfigurationKeys.MODULE_NAME, "main")
        config.addAll(CLIConfigurationKeys.CONTENT_ROOTS, classpath.map { JvmClasspathRoot(File(it)) })
        if (manageImports) {
            config.add(CLIConfigurationKeys.CONTENT_ROOTS, JavaSourceRoot(sourceRoot.toFile(), ""))
        }
        config.put(
            CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
            PrintingMessageCollector(System.err, MessageRenderer.GRADLE_STYLE, true)
        )

        val environment = KotlinCoreEnvironment.createForProduction(
            disposable,
            config,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
        TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
            environment.project,
            emptyList(),
            NoScopeRecordCliBindingTrace(),
            environment.configuration,
            { scope: GlobalSearchScope -> environment.createPackagePartProvider(scope) }
        )
        return environment
    }

    companion object {

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val mappings =
                File("C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HollowCore\\src\\main\\resources\\mappings.tsrg").toPath()
                    .readMappings()
            val transformer = Transformer(mappings).apply {
                fun findClasspathEntry(cls: String): String {
                    val classFilePath = "/${cls.replace('.', '/')}.class"
                    val url = javaClass.getResource(classFilePath)
                        ?: throw RuntimeException("Failed to find $cls on classpath.")

                    return when {
                        url.protocol == "jar" && url.file.endsWith("!$classFilePath") -> {
                            Paths.get(URL(url.file.removeSuffix("!$classFilePath")).toURI()).absolutePathString()
                        }

                        url.protocol == "file" && url.file.endsWith(classFilePath) -> {
                            var path = Paths.get(url.toURI())
                            repeat(cls.count { it == '.' } + 1) {
                                path = path.parent
                            }
                            path.absolutePathString()
                        }

                        else -> {
                            throw RuntimeException("Do not know how to turn $url into classpath entry.")
                        }
                    }
                }
                classpath = arrayOf(
                    findClasspathEntry("net.minecraft.client.Minecraft"),
                    "C:\\Users\\user\\Twitch\\Minecraft\\Instances\\Instances\\NineWorlds\\mods\\hc-1.0-all.jar",
                    "C:\\Users\\user\\Twitch\\Minecraft\\Instances\\Instances\\NineWorlds\\mods\\hollowstory-1.0.jar",
                    "C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HS\\libs\\guardvillagers-1.2.6.jar",
                )
            }

            fun remap(file: File) {
                println(
                    transformer.remap(
                        mapOf(
                            "test.kt" to file.readText()
                        )
                    )["test.kt"]!!.first
                )
                println()
            }

            //remap(File("C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HS\\src\\main\\resources\\assets\\hollowstory\\hevents\\bandits.se.kt"))
            remap(File("C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HS\\src\\main\\resources\\assets\\hollowstory\\hevents\\village.se.kt"))

        }
    }

}

fun Path.readMappings(): MappingSet {
    val name = fileName.toString()
    val ext = name.substring(name.lastIndexOf(".") + 1)
    val format = MappingFormats.REGISTRY.values().find { it.standardFileExtension.orElse(null) == ext }
        ?: throw UnsupportedOperationException("Cannot find mapping format for $this")
    return format.read(this)
}