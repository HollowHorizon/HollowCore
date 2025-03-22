package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.pipeline.ClearColorDontCare
import de.fabmax.kool.pipeline.ClearDepthDontCare
import de.fabmax.kool.pipeline.ClearDepthLoad
import de.fabmax.kool.scene.Scene
import net.minecraft.client.gui.screens.Screen
import ru.hollowhorizon.hc.api.HudHideable
import ru.hollowhorizon.hc.common.utils.literal

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
        clearColor = ClearColorDontCare
        clearDepth = ClearDepthDontCare
        isVisible = false
    }
}