package ru.hollowhorizon.hc.common.kotlin


import net.minecraft.client.gui.screen.Screen
import ru.hollowhorizon.hc.client.utils.open
import java.io.InputStream
import java.io.InputStreamReader
import javax.script.ScriptEngine

object KotlinEngine {
    //val engine: () -> ScriptEngine = { ScriptEngineManager().getEngineByExtension("kts") }

    fun createMCScreen(engine: ScriptEngine, screen: Screen, stream: InputStream) {
        println("Creating MC screen")

        engine.put("screen", screen)
        engine.put("width", screen.width)
        engine.put("height", screen.height)

        println("Loading script")
        engine.eval(InputStreamReader(stream, Charsets.UTF_8))
        println("Done")

        screen.open()
    }


}