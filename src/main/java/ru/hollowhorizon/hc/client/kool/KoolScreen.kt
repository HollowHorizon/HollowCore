package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.modules.ui2.setupUiScene
import de.fabmax.kool.pipeline.ClearColorDontCare
import de.fabmax.kool.pipeline.ClearDepthDontCare
import de.fabmax.kool.pipeline.ClearDepthLoad
import de.fabmax.kool.scene.OrthographicCamera
import de.fabmax.kool.scene.Scene
import net.minecraft.client.gui.screens.Screen
import ru.hollowhorizon.hc.api.HudHideable
import ru.hollowhorizon.hc.common.utils.literal

open class KoolScreen : Screen("".literal), HudHideable {
    val scene = Scene(title.string).apply {
        setupUiScene()
        clearColor = ClearColorDontCare
        clearDepth = ClearDepthDontCare
    }

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
        scene.release()
    }
}

fun Scene.isScreenScene(): Boolean {
    (camera as? OrthographicCamera)?.let { cam ->
        if(cam.left != 0f) return false
        if(cam.top != 0f) return false
        val viewport = mainRenderPass.viewport
        if(cam.right != viewport.width.toFloat()) return false
        if(cam.bottom != -viewport.height.toFloat()) return false
    } ?: return false
    return true
}