package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.KoolApplication
import de.fabmax.kool.addScene
import de.fabmax.kool.modules.ksl.KslUnlitShader
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.pipeline.*
import de.fabmax.kool.pipeline.FullscreenShaderUtil.fullscreenQuadVertexStage
import de.fabmax.kool.pipeline.FullscreenShaderUtil.generateFullscreenQuad
import de.fabmax.kool.pipeline.backend.gl.GlTexture
import de.fabmax.kool.pipeline.backend.gl.LoadedTextureGl
import de.fabmax.kool.scene.addTextureMesh
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.MdColor
import net.minecraft.client.Minecraft

fun main() = KoolApplication { createExampleScene() }

private val resolvedColor = Texture2d(
    TextureProps(
        generateMipMaps = false,
        defaultSamplerSettings = SamplerSettings().clamped().nearest()
    )
).apply {
    val estSize =
        Texture.estimatedTexSize(Minecraft.getInstance().window.width, Minecraft.getInstance().window.height, 1, 1, 4)
            .toLong()
    gpuTexture = LoadedTextureGl(
        MCGlApi.TEXTURE_2D,
        GlTexture(Minecraft.getInstance().mainRenderTarget.colorTextureId),
        MCGlApi.backend,
        this,
        estSize
    ).apply {
        width = Minecraft.getInstance().window.width
        height = Minecraft.getInstance().window.height
    }
    loadingState = Texture.LoadingState.LOADED
}


fun KoolApplication.createExampleScene() {
    addScene {
        setupUiScene()

        addTextureMesh {
            generateFullscreenQuad()
            shader = KslUnlitShader {
                pipeline { depthTest = DepthCompareOp.ALWAYS }
                color { textureData(resolvedColor) }
                modelCustomizer = { fullscreenQuadVertexStage(null) }
            }
        }

        addPanelSurface(colors = Colors.singleColorLight(MdColor.LIGHT_GREEN)) {
            modifier
                .size(400.dp, 300.dp)
                .align(AlignmentX.Center, AlignmentY.Center)
                .background(RoundRectBackground(colors.background, 16.dp))

            var clickCount by remember(0)
            Button("Click me!") {
                modifier
                    .alignX(AlignmentX.Center)
                    .margin(sizes.largeGap * 4f)
                    .padding(horizontal = sizes.largeGap, vertical = sizes.gap)
                    .font(sizes.largeText)
                    .onClick { clickCount++ }
            }
            Text("Button clicked $clickCount times") {
                modifier
                    .alignX(AlignmentX.Center)
            }
        }
    }
}