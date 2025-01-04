package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.Assets
import de.fabmax.kool.loadImage2d
import de.fabmax.kool.math.Vec2i
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.pipeline.SamplerSettings
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.pipeline.TextureProps

fun UiScope.Image(location: String, block: ImageScope.() -> Unit = {}) = Image {
    modifier.image(remember {
        Texture2d(
            TextureProps(
                generateMipMaps = false,
                defaultSamplerSettings = SamplerSettings().clamped().nearest()
            )
        ) {
            Assets.loadImage2d(location).getOrThrow()
        }
    })

    block()
}