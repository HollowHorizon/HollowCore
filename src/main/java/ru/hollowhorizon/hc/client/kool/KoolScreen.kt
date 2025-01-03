package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.scene.Scene
import net.minecraft.client.gui.screens.Screen
import ru.hollowhorizon.hc.api.HudHideable
import ru.hollowhorizon.hc.client.utils.literal

open class KoolScreen(builder: Scene.() -> Unit) : Screen("".literal), HudHideable {
    val scene = Scene(title.string).apply(builder).apply {
        KoolManager.context.addScene(this)
    }

    override fun onClose() {
        super.onClose()
        KoolManager.context.removeScene(scene)
    }
}