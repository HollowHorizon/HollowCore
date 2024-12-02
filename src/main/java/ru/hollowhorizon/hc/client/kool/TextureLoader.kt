package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.platform.NativeImage
import com.tianscar.imageio.plugins.png.PNGImageReader
import com.tianscar.imageio.plugins.png.PNGImageReaderSpi
import com.tianscar.imageio.plugins.png.PNGMetadata
import de.fabmax.kool.pipeline.TexFormat
import de.fabmax.kool.pipeline.TextureData2d
import de.fabmax.kool.pipeline.TextureProps
import de.fabmax.kool.util.Buffer
import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.client.textures.getFrameRate
import ru.hollowhorizon.hc.client.utils.HollowColor
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO

object TextureLoader {
    fun load(stream: InputStream, ext: String, props: TextureProps?): TextureData2d {

        val image = decode(stream, ext, props)

        return TextureData2d(
            NativeImageBuffer(image),
            image.width, image.height, props?.format ?: TexFormat.RGBA
        )
    }

    private fun decode(textures: InputStream, ext: String, props: TextureProps?): NativeImage {
        val reader =
            if (ext == "png") PNGImageReader(PNGImageReaderSpi()) else ImageIO.getImageReadersByFormatName(ext).next()
        reader.input = ImageIO.createImageInputStream(textures)
        val framesCount = reader.getNumImages(true)
        val images = (0 until framesCount).map(reader::read)
        if (images.isEmpty()) throw IllegalStateException("Image is empty!")

        val frame = images[0]
        val framerate = reader.getImageMetadata(0).getFrameRate().toFloat()
        val frameWidth = frame.width
        val frameHeight = frame.height
        val nImage = NativeImage(frameWidth, frameHeight * framesCount, Minecraft.ON_OSX)
        for ((i, image) in images.withIndex()) {
            val width = image.width
            val height = image.height
            val xOffset = (reader.getImageMetadata(i) as? PNGMetadata)?.fcTL_x_offset ?: 0
            val yOffset = (reader.getImageMetadata(i) as? PNGMetadata)?.fcTL_y_offset ?: 0

            for (y in 0 until height) {
                for (x in 0 until width) {
                    nImage.setPixelRGBA(x + xOffset, yOffset + y + i * frameHeight, image.getRGB(x, y))
                }
            }
        }

        return nImage
    }
}

class NativeImageBuffer(
    val image: NativeImage,
) : Buffer {
    override val capacity: Int get() = -1
    override var isAutoLimit: Boolean
        get() = false
        set(value) {}
    override var limit: Int
        get() = -1
        set(value) {}
    override var position: Int
        get() = -1
        set(value) {}

    override fun clear() {
        image.close()
    }

}