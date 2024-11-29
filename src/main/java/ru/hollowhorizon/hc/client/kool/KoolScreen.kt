package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.KoolSystem
import de.fabmax.kool.scene.Scene
import net.minecraft.client.gui.screens.Screen
import ru.hollowhorizon.hc.client.utils.literal

class KoolScreen(builder: Scene.() -> Unit) : Screen("".literal) {
    val scene = Scene(title.string).apply(builder)

    init {
        KoolSystem.requireContext().addScene(scene)
    }

    override fun onClose() {
        super.onClose()
        KoolSystem.requireContext().removeScene(scene)
    }
}