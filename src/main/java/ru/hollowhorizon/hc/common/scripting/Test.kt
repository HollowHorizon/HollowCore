package ru.hollowhorizon.hc.common.scripting

import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLLoader
import org.jetbrains.kotlin.fir.lightTree.LightTree2Fir
import org.jetbrains.kotlin.parsing.KotlinExpressionParsing
import org.jetbrains.kotlin.parsing.KotlinParsing
import ru.hollowhorizon.hc.common.scripting.classloader.HollowScriptClassLoader
import java.io.File
import kotlin.script.experimental.jvm.baseClassLoader
import kotlin.script.experimental.jvm.jvm
import kotlin.script.experimental.jvm.loadDependencies

object Test {
    @JvmStatic
    fun main() {
        if(FMLLoader.isProduction()) System.setProperty("kotlin.java.stdlib.jar", ModList.get().getModFileById("hc").file.filePath.toFile().absolutePath)
        val compiled = HSCompiler().compile<HollowScript>(
            "test",
            File("C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HollowCore\\src\\main\\resources\\assets\\hc\\screen\\test.hs.kts").inputStream()
        )

        println("Script compiled: ${compiled.scriptName}")

        val res = compiled.execute {
            jvm {
                loadDependencies(false)
            }
        }

        res.reports.forEach {
            println(it.render())
        }
    }

    @JvmStatic
    fun test() {
        val text = LightTree2Fir.Companion.buildLightTree(File("C:\\Users\\user\\Desktop\\papka_with_papkami\\MyJavaProjects\\HollowCore\\src\\main\\resources\\assets\\hc\\screen\\test.hs.kts").readText())


        println(text)
    }
}