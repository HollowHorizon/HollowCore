package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.scene.Scene
import net.minecraft.client.gui.screens.Screen
import ru.hollowhorizon.hc.api.HudHideable
import ru.hollowhorizon.hc.common.utils.literal

open class KoolScreen(builder: Scene.() -> Unit) : Screen("".literal), HudHideable {
    val scene = ScreenScene(title.string).apply(builder)

    override fun added() {
        KoolManager.context.addScene(scene)
    }

    override fun removed() {
        KoolManager.context.removeScene(scene)
    }
}

class ScreenScene(name: String? = null): Scene(name) {
    init {
        clearColor = null
        clearDepth = false
        isVisible = false
    }
}