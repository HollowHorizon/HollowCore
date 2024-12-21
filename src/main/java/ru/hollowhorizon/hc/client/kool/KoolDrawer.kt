package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.platform.GlStateManager
import de.fabmax.kool.Assets
import de.fabmax.kool.KoolApplication
import de.fabmax.kool.addScene
import de.fabmax.kool.modules.gltf.loadGltfModel
import de.fabmax.kool.pipeline.CullMethod
import de.fabmax.kool.pipeline.DepthCompareOp
import de.fabmax.kool.pipeline.backend.gl.glOp
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.scene.defaultOrbitCamera
import de.fabmax.kool.util.launchOnMainThread
import org.lwjgl.opengl.GL33

// Scene in game
val EXAMPLE_SCENE by lazy {
    KoolDrawer {
        clearColor = null
        clearDepth = false

        mcCamera()

        launchOnMainThread {
            val model = Assets.loadGltfModel("hollowcore:models/helmet.glb").getOrThrow()

            addNode(model)
        }
    }
}

// Default kool scene
fun main() = KoolApplication {
    addScene {
        defaultOrbitCamera()

        launchOnMainThread {
            val model = Assets.loadGltfModel("https://github.com/KhronosGroup/glTF-Sample-Models/raw/refs/heads/main/2.0/DamagedHelmet/glTF-Binary/DamagedHelmet.glb").getOrThrow()

            addNode(model)
        }
    }
}

open class KoolDrawer(builder: Scene.() -> Unit) {
    val scene = Scene("Ingame Drawer").apply(builder).apply {
        KoolManager.ctx.addScene(this)
    }
    var actIsWriteDepth = true
    var actDepthTest: DepthCompareOp = DepthCompareOp.LESS_EQUAL
    var actCullMethod: CullMethod = CullMethod.NO_CULLING
    var lineWidth = 1f

    fun draw() {
        //MCGlApi.clipControl(MCGlApi.LOWER_LEFT, MCGlApi.ZERO_TO_ONE)
        MCGlApi.clipControl(MCGlApi.LOWER_LEFT, MCGlApi.NEGATIVE_ONE_TO_ONE)
        val activeTexture = GlStateManager._getActiveTexture()
        val currentTexture = GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D)
        val currentVAO = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING)
        val currentElementArrayBuffer = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING)

        MCGlApi.depthMask(actIsWriteDepth)
        if (actDepthTest == DepthCompareOp.ALWAYS) {
            MCGlApi.disable(MCGlApi.DEPTH_TEST)
        } else {
            MCGlApi.enable(MCGlApi.DEPTH_TEST)
            MCGlApi.depthFunc(actDepthTest.glOp(MCGlApi))
        }
        when (actCullMethod) {
            CullMethod.CULL_BACK_FACES -> {
                MCGlApi.enable(MCGlApi.CULL_FACE)
                MCGlApi.cullFace(MCGlApi.BACK)
            }

            CullMethod.CULL_FRONT_FACES -> {
                MCGlApi.enable(MCGlApi.CULL_FACE)
                MCGlApi.cullFace(MCGlApi.FRONT)
            }

            CullMethod.NO_CULLING -> MCGlApi.disable(MCGlApi.CULL_FACE)
        }
        MCGlApi.lineWidth(lineWidth)

        KoolManager.ctx.renderFrame()

        // Необходимо сохранять эти параметры, поскольку и майн и движок кешируют их
        actIsWriteDepth = GL33.glGetBoolean(GL33.GL_DEPTH_WRITEMASK)
        actDepthTest = getCurrentDepthCompareOp()
        actCullMethod = getCurrentCullMethod()
        lineWidth = GL33.glGetFloat(GL33.GL_LINE_WIDTH)

        GL33.glActiveTexture(activeTexture)
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, currentTexture)
        GL33.glBindVertexArray(currentVAO)
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)
        //MCGlApi.clipControl(MCGlApi.LOWER_LEFT, MCGlApi.NEGATIVE_ONE_TO_ONE)

        GL33.glEnable(GL33.GL_DEPTH_TEST)
        GL33.glDepthFunc(GL33.GL_LEQUAL)
        GL33.glEnable(GL33.GL_BLEND)
        GL33.glBlendFuncSeparate(
            GL33.GL_SRC_ALPHA,
            GL33.GL_ONE_MINUS_SRC_ALPHA,
            GL33.GL_ONE,
            GL33.GL_ONE_MINUS_SRC_ALPHA
        )
        GL33.glEnable(GL33.GL_CULL_FACE)
    }
}