package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.KoolApplication
import de.fabmax.kool.addScene
import de.fabmax.kool.math.Vec3f
import de.fabmax.kool.math.deg
import de.fabmax.kool.modules.ksl.KslPbrShader
import de.fabmax.kool.modules.ksl.KslUnlitShader
import de.fabmax.kool.modules.ksl.lang.xy
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.pipeline.DepthCompareOp
import de.fabmax.kool.pipeline.FullscreenShaderUtil.fullscreenQuadVertexStage
import de.fabmax.kool.pipeline.FullscreenShaderUtil.generateFullscreenQuad
import de.fabmax.kool.scene.addColorMesh
import de.fabmax.kool.scene.addTextureMesh
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.Time
import ru.hollowhorizon.hc.client.imgui.MINECRAFT_BUFFER

fun main() = KoolApplication { minecraftScene() }


fun KoolApplication.minecraftScene() {
    addScene {
        val screen = addTextureMesh {
            generateFullscreenQuad()
            shader = KslUnlitShader {
                pipeline { depthTest = DepthCompareOp.ALWAYS }
                color { textureData(MINECRAFT_BUFFER) }
                modelCustomizer = {
                    vertexStage {
                        main {
                            outPosition set float4Value(vertexAttribFloat3(Attribute.POSITIONS.name).xy, 1f, 1f)
                        }
                    }
                }
            }
        }

        mcCamera()

        addColorMesh {
            generate {
                cube {
                    colored()
                }
            }
            shader = KslPbrShader {
                color { vertexColor() }
                metallic(0f)
                roughness(0.25f)
            }
            onUpdate {
                transform.rotate(45f.deg * Time.deltaT, Vec3f.X_AXIS)
            }
        }

        lighting.singleDirectionalLight {
            setup(Vec3f(-1f, -1f, -1f))
            setColor(Color.WHITE, 5f)
        }
    }
}