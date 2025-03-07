package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.scene.Scene
import net.minecraft.client.gui.screens.Screen
import ru.hollowhorizon.hc.api.HudHideable
import ru.hollowhorizon.hc.client.utils.literal

open class KoolScreen : Screen("".literal), HudHideable {
    val scene = ScreenScene(title.string)

    private var isLoaded = false
    override fun init() {
        if(!isLoaded) {
            scene.setup()
            isLoaded = true
        }
        super.init()
    }

    open fun Scene.setup() {}

    override fun added() {
        KoolManager.context.addScene(scene)
    }

    override fun removed() {
        KoolManager.context.removeScene(scene)
    }
}

open class ScreenScene(name: String? = null): Scene(name) {
    init {
        clearColor = null
        clearDepth = false
        isVisible = false
    }
}