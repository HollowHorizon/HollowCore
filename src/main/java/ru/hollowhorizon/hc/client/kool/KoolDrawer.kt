package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.platform.GlStateManager
import de.fabmax.kool.Assets
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.modules.gizmo.GizmoMode
import de.fabmax.kool.modules.gizmo.SimpleGizmo
import de.fabmax.kool.modules.gltf.GltfLoadConfig
import de.fabmax.kool.modules.gltf.GltfMaterialConfig
import de.fabmax.kool.modules.gltf.loadGltfModel
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.modules.ui2.docking.UiDockable
import de.fabmax.kool.pipeline.CullMethod
import de.fabmax.kool.pipeline.DepthCompareOp
import de.fabmax.kool.pipeline.ao.AoPipeline
import de.fabmax.kool.pipeline.backend.gl.glOp
import de.fabmax.kool.scene.Mesh
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.util.CascadedShadowMap
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.launchDelayed
import de.fabmax.kool.util.launchOnMainThread
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.client.kool.gl.KslPbrShaderLightmap

// Scene in game
val GAME_SCENE = KoolDrawer {
    clearColor = null
    clearDepth = false

    mcCamera()

    lighting.singleDirectionalLight {
        setup(Vec3f(-1f, -1f, -1f))
        setColor(Color.WHITE, 1.5f)
    }

    val shadowMap = CascadedShadowMap(this, lighting.lights[0]).apply {
        setMapRanges(0.035f, 0.17f, 1f)
        subMaps.forEach {
            it.directionalCamNearOffset = -200f
            it.setDefaultDepthOffset(true)
        }
    }
    val aoPipeline = AoPipeline.createForward(this).apply {
        radius = 0.55f
        power = 0.1f
        strength = 1.25f
        kernelSz = 8
    }

    shader = KslPbrShaderLightmap(shadowMap, aoPipeline)

    launchOnMainThread {
        val model = Assets.loadGltfModel(
            "hollowcore:models/helmet.glb", GltfLoadConfig(
                materialConfig = GltfMaterialConfig(
                    shadowMaps = listOf(shadowMap),
                    scrSpcAmbientOcclusionMap = aoPipeline.aoMap
                )
            )
        ).getOrThrow()

        val gizmo = SimpleGizmo(hideHandlesOnDrag = false).apply {
            gizmoNode.gizmoTransform.scale(2.5f)
            gizmoNode.transform.scale(2.5f)
        }

        val transformMode = mutableStateOf(gizmo.mode).onChange { _, new -> gizmo.mode = new }

        fun UiScope.gizmoMode(mode: GizmoMode, label: String) = Row(Grow.Std) {
            RadioButton(transformMode.use() == mode) {
                modifier
                    .alignY(AlignmentY.Center)
                    .margin(end = sizes.gap)
                    .onToggle {
                        if (it) {
                            transformMode.set(mode)
                        }
                    }
            }
            Text(label) {
                modifier.width(Grow.Std)
                    .onClick { transformMode.set(mode) }
            }
        }

        KoolManager.ctx.addScene(UiScene {
            val dockable = UiDockable("Settings")
            addWindowSurface(dockable) {
                Column {
                    TitleBar(dockable)
                    Column {
                        modifier.margin(sizes.gap)
                        gizmoMode(GizmoMode.TRANSLATE, "Translate")
                        gizmoMode(GizmoMode.ROTATE, "Rotate")
                        gizmoMode(GizmoMode.SCALE, "Scale")
                    }
                }
            }
        })

        addNode(gizmo)
        addNode(model)

        launchDelayed(1) {
            gizmo.setTransformNode(model)
        }
    }
}

lateinit var shader: KslPbrShaderLightmap
var currentChunkMesh: Mesh? = null

open class KoolDrawer(builder: Scene.() -> Unit) {
    val scene = Scene("Ingame Drawer").apply(builder).apply {
        KoolManager.ctx.addScene(this)
    }
    var actIsWriteDepth = true
    var actDepthTest: DepthCompareOp = DepthCompareOp.LESS_EQUAL
    var actCullMethod: CullMethod = CullMethod.NO_CULLING
    var lineWidth = 1f

    fun draw() {
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
        GL33.glCullFace(GL33.GL_BACK)
        GL33.glEnable(GL33.GL_CULL_FACE)
    }
}