package ru.hollowhorizon.hc.client.gui

import de.fabmax.kool.Assets
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.modules.gltf.loadGltfModel
import de.fabmax.kool.modules.ksl.KslPbrShader
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.pipeline.*
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.scene.addColorMesh
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.MsdfFont
import de.fabmax.kool.util.Time
import de.fabmax.kool.util.launchOnMainThread
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.kool.KoolManager
import ru.hollowhorizon.hc.client.kool.minecraft.mcCamera
import ru.hollowhorizon.hc.common.capabilities.SyncableMapImpl

object DebugOverlay {
    val debugText = SyncableMapImpl.create<String, String>(HashMap()) {
        surface.triggerUpdate()
    }
    private val debugScene = Scene().apply {
        setupUiScene()
        clearColor = ClearColorDontCare
        clearDepth = ClearDepthDontCare

        surface = addPanelSurface(sizes = Sizes.medium(normalText = MsdfFont(KoolManager.MONOCRAFT))) {
            modifier.size(FitContent, FitContent)
                .align(AlignmentX.End, AlignmentY.Top)

            debugText.forEach { (key, value) ->
                Text("${key}: $value") {}
            }
        }

        onUpdate {
            isVisible = debugText.isNotEmpty()
        }
    }
    private var surface: UiSurface

    internal fun init() {
        if (HollowCore.config.debugMode) KoolManager.context.addScene(debugScene)
        KoolManager.context.addScene(Scene().apply {
            mainRenderPass.depthMode = DepthMode.Legacy
            clearColor = ClearColorLoad
            clearDepth = ClearDepthLoad

            lighting.singleDirectionalLight {
                setup(Vec3f(-0.8f, -1.2f, -1f)).setColor(Color.WHITE, 5f)
            }
            mcCamera()

//            addColorMesh {
//                generate {
//                    cube {
//                        colored()
//                    }
//                }
//                shader = KslPbrShader {
//                    color { vertexColor() }
//                    metallic(0f)
//                    roughness(0.25f)
//                }
//            }.transform.translate(8.5, -60.0, 8.5)

            launchOnMainThread {
                val model = Assets.loadGltfModel("hollowcore:models/entity/scene.gltf").getOrThrow()

                model.enableAnimation(0)
                model.transform.translate(8.5, -60.0, 8.5)
                addNode(model)

                onUpdate {
                    model.applyAnimation(Time.deltaT)
                }
            }
        })
    }
}