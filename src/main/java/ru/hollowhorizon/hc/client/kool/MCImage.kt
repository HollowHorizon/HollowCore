package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.Assets
import de.fabmax.kool.math.Vec2i
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.pipeline.AsyncTextureLoader
import de.fabmax.kool.pipeline.SamplerSettings
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.pipeline.TextureProps
import net.minecraft.resources.ResourceLocation

val IMAGE_SIZES = hashMapOf<Int, Vec2i>()

fun UiScope.Image(location: String, block: ImageScope.() -> Unit = {}) = Image {
    modifier.image(remember { Texture2d(
        TextureProps(
            generateMipMaps = false,
            defaultSamplerSettings = SamplerSettings().clamped().nearest()
        ),
        loader = AsyncTextureLoader {
            Assets.loadTextureData(location)
        }
    ) })

    block()
}