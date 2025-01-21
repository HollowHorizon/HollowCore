package ru.hollowhorizon.hc.client.kool.gl

import de.fabmax.kool.modules.ksl.KslPbrShader
import de.fabmax.kool.modules.ksl.ShadowMapConfig
import de.fabmax.kool.modules.ksl.lang.div
import de.fabmax.kool.modules.ksl.lang.getFloat4Port
import de.fabmax.kool.modules.ksl.lang.times
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.pipeline.Texture
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.pipeline.ao.AoPipeline
import de.fabmax.kool.pipeline.backend.gl.GlTexture
import de.fabmax.kool.pipeline.backend.gl.LoadedTextureGl
import de.fabmax.kool.scene.Mesh
import de.fabmax.kool.scene.MeshInstanceList
import de.fabmax.kool.scene.Node
import de.fabmax.kool.scene.geometry.IndexedVertexList
import de.fabmax.kool.scene.geometry.PrimitiveType
import de.fabmax.kool.scene.geometry.VertexView
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.ShadowMap
import ru.hollowhorizon.hc.client.kool.glTexture
import ru.hollowhorizon.hc.client.kool.MCGlApi
import ru.hollowhorizon.hc.client.kool.support.MC_UV_1
import ru.hollowhorizon.hc.client.kool.support.MC_UV_2
import ru.hollowhorizon.hc.client.models.internal.manager.GltfManager

class KslPbrShaderLightmap(shadowMap: ShadowMap, aoPipeline: AoPipeline) : KslPbrShader({
    color {
        textureColor(Texture2d().apply {
            gpuTexture = LoadedTextureGl(
                MCGlApi.TEXTURE_2D,
                GlTexture(GltfManager.blockAtlasId),
                MCGlApi.backend,
                this,
                0L
            )
            loadingState = Texture.LoadingState.LOADED
        })
    }
    lighting {
        ambientLight = AmbientLight.Uniform(Color(0.5f, 0.5f, 0.5f).toLinear())
        addShadowMap(shadowMap, ShadowMapConfig.SHADOW_SAMPLE_PATTERN_4x4)
        enableSsao(aoPipeline.aoMap)
    }

    modelCustomizer = {
        val lightMapColor = interStageFloat4()

        vertexStage {
            val UV2 = vertexAttribInt2("UV2")

            main {
                lightMapColor.input set texelFetch(texture2d("lightmap"), UV2 / 16.const, 0.const)
            }
        }

        fragmentStage {
            main {
                val materialColorPort = getFloat4Port("materialColor")
                val materialColor = materialColorPort.input.input!!
                val materialColorMod = float4Var(materialColor)
                materialColorMod set materialColorMod * lightMapColor.output
                materialColorPort.input(materialColorMod)
            }
        }
    }
}) {
    private val lightmap by texture2d("lightmap", glTexture(GltfManager.lightTextureId))
}

fun Node.addTextureMeshWithLightmap(
    name: String = "mesh",
    isNormalMapped: Boolean = false,
    instances: MeshInstanceList? = null,
    primitiveType: PrimitiveType = PrimitiveType.TRIANGLES,
    block: Mesh.() -> Unit,
): Mesh {
    val attributes = mutableListOf(Attribute.POSITIONS, Attribute.NORMALS, Attribute.COLORS, Attribute.TEXTURE_COORDS, MC_UV_2)
    if (isNormalMapped) {
        attributes += Attribute.TANGENTS
    }

    val mesh = Mesh(IndexedVertexList(attributes, primitiveType), instances, name=makeChildName(name))
    addNode(mesh)
    if (isNormalMapped) {
        mesh.geometry.generateTangents()
    }
    return mesh
}

val VertexView.lightMapUV get() = getVec2iAttribute(MC_UV_2) ?: error("Lightmap Coords not found!")
val VertexView.overlayUV get() = getVec2iAttribute(MC_UV_1) ?: error("Overlay Coords not found")