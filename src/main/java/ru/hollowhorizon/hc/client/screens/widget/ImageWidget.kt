package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.NativeImage
import net.minecraft.client.renderer.texture.Texture
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.util.ResourceLocation
import ru.hollowhorizon.hc.client.utils.toSTC
import ru.hollowhorizon.hc.client.utils.toTexture
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

class ImageWidget(x: Int, y: Int, width: Int, height: Int) : HollowWidget(x, y, width, height, "IMAGE_WIDGET".toSTC()) {
    private var image: Texture = TextureManager.INTENTIONAL_MISSING_TEXTURE.toTexture()

    override fun render(p_230430_1_: MatrixStack, p_230430_2_: Int, p_230430_3_: Int, p_230430_4_: Float) {
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_)
    }

    fun setImage(image: CompletableFuture<Texture>) {
        image.thenAcceptAsync {
            this.image = it
        }
    }

    fun setImage(image: ResourceLocation) {
        setImage(CompletableFuture.supplyAsync { image.toTexture() })
    }

    fun setImage(image: URL) {
        setImage(CompletableFuture.supplyAsync {
            val connection = image.openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.useCaches = true
            connection.addRequestProperty("User-Agent", "Mozilla/4.76 (Elementa)")
            connection.doOutput = true

            return@supplyAsync DynamicTexture(NativeImage.read(connection.inputStream))
        })
    }

    companion object {
        fun ofResource(image: ResourceLocation, x: Int, y: Int, width: Int, height: Int): ImageWidget {
            return ImageWidget(x, y, width, height).apply {
                setImage(image)
            }
        }

        fun ofURL(url: URL, x: Int, y: Int, width: Int, height: Int): ImageWidget {
            return ImageWidget(x, y, width, height).apply {
                setImage(url)
            }
        }
    }
}