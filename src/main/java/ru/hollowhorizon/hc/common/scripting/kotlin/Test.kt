package ru.hollowhorizon.hc.common.scripting.kotlin

import kotlinx.coroutines.runBlocking
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.rl
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream
import java.util.jar.Manifest
import kotlin.script.experimental.annotations.KotlinScript
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvm.BasicJvmScriptEvaluator
import kotlin.script.experimental.jvm.defaultJvmScriptingHostConfiguration
import kotlin.script.experimental.jvm.impl.*
import kotlin.script.experimental.jvmhost.JvmScriptCompiler
import kotlin.script.experimental.jvmhost.createJvmCompilationConfigurationFromTemplate

@KotlinScript(
    compilationConfiguration = HollowScriptConfiguration::class
)
abstract class Script


fun main() {
    ForgeRegistries.ITEMS.getValue("my_super_mod:my_item2".rl)!!.descriptionId

    val text = """
            package ru.hollow.test
            
            import com.mojang.blaze3d.matrix.MatrixStack
            import net.minecraft.client.gui.screen.Screen
            import ru.hollowhorizon.hc.client.utils.mcText
            import net.minecraft.client.Minecraft
            
            class Test: Screen("".mcText) {
                val textureManager = Minecraft.getInstance().textureManager
            
                override fun init() {
                    super.init()
                }
            
                override fun render(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
                    super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks)
                }
            }
            
            val test = Test()
        """.trimIndent()

    val hostConfiguration = defaultJvmScriptingHostConfiguration

    val compiler = JvmScriptCompiler(hostConfiguration)

    runBlocking {

        try {
            val compiled = compiler(
                text.toScriptSource(),
                createJvmCompilationConfigurationFromTemplate<Script>()
            ).also { result ->
                result.reports.forEach { HollowCore.LOGGER.info("Compile Info: {}", it.render(withStackTrace = true)) }
            }.valueOrThrow()

            val evaluator = BasicJvmScriptEvaluator()
            val exec = runBlocking { evaluator(compiled, ScriptEvaluationConfiguration {}) }.also { result ->
                result.reports.forEach { HollowCore.LOGGER.info("Eval Info: {}", it.render(withStackTrace = true)) }
            }.valueOrThrow()

            (compiled as KJvmCompiledScript).saveScriptToJar(File("test.jar"))
            HollowCore.LOGGER.info(exec)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun KJvmCompiledScript.saveScriptToJar(outputJar: File) {
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