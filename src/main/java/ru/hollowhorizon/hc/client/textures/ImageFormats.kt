package ru.hollowhorizon.hc.client.textures

import com.mojang.blaze3d.Blaze3D
import com.mojang.blaze3d.platform.NativeImage
import com.tianscar.imageio.plugins.png.PNGImageReader
import com.tianscar.imageio.plugins.png.PNGImageReaderSpi
import com.tianscar.imageio.plugins.png.PNGMetadata
import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.SimpleTexture
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.imgui.Graphics
import ru.hollowhorizon.hc.client.utils.HollowColor
import ru.hollowhorizon.hc.client.utils.stream
import java.awt.image.BufferedImage
import java.io.InputStream
import javax.imageio.ImageIO
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode
import kotlin.properties.Delegates

val IMAGES = HashMap<ResourceLocation, ImageFormat>()

class ImageFormat(val location: ResourceLocation) : SimpleTexture(location) {
    var framesCount by Delegates.notNull<Int>()
    var width by Delegates.notNull<Int>()
    var height by Delegates.notNull<Int>()
    var framerate by Delegates.notNull<Float>()
    private val texID: Int

    init {
        val image = decode(location.stream)
        val texture = DynamicTexture(image)
        //texture.upload()
        texID = texture.id
    }

    private fun decode(textures: InputStream): NativeImage {
        val ext = location.path.substringAfterLast('.')

        val reader =
            if (ext == "png") PNGImageReader(PNGImageReaderSpi()) else ImageIO.getImageReadersByFormatName(ext).next()
        reader.input = ImageIO.createImageInputStream(textures)
        framesCount = reader.getNumImages(true)
        val images = (0 until framesCount).map(reader::read)
        if (images.isEmpty()) throw IllegalStateException("Image is empty!")

        val frame = images[0]
        framerate = reader.getImageMetadata(0).getFrameRate().toFloat()
        width = frame.width
        height = frame.height
        val nImage = NativeImage(width, height * framesCount, Minecraft.ON_OSX)
        val pixels = IntArray(width * height)
        for ((i, image) in images.withIndex()) {
            val width = image.width
            val height = image.height
            val xOffset = (reader.getImageMetadata(i) as? PNGMetadata)?.fcTL_x_offset ?: 0
            val yOffset = (reader.getImageMetadata(i) as? PNGMetadata)?.fcTL_y_offset ?: 0

            if (image.type == BufferedImage.TYPE_INT_ARGB || image.type == BufferedImage.TYPE_INT_RGB) {
                image.raster.getDataElements(0, 0, width, height, pixels)
            } else {
                image.getRGB(0, 0, width, height, pixels, 0, width)
            }

            for (y in 0 until height) {
                for (x in 0 until width) {
                    val pixel = pixels[y * width + x]
                    val r = (((pixel shr 16) and 0xFF)) // R
                    val g = (((pixel shr 8) and 0xFF)) // G
                    val b = ((pixel and 0xFF)) // B
                    val a = (((pixel shr 24) and 0xFF)) // A
                    nImage.setPixelRGBA(x + xOffset, yOffset + y + i * this.height, HollowColor(a, b, g, r).toABGR())
                }
            }
        }

        return nImage
    }

    override fun getId() = texID
}

fun Graphics.drawImage(location: ResourceLocation, width: Float, height: Float) {
    val size = ImVec2(width, height)
    val image = IMAGES.computeIfAbsent(location, ::ImageFormat)

    val currentFrame = (Blaze3D.getTime() * image.framerate).toInt() % (image.framesCount - 1).coerceAtLeast(1)
    val y0 = currentFrame / image.framesCount.toFloat()
    val y1 = (currentFrame + 1) / image.framesCount.toFloat()


    ImGui.image(
        image.id,
        size, ImVec2(0f, y0), ImVec2(1f, y1),
        ImVec4(1f, 1f, 1f, 1f), ImVec4(1f, 1f, 1f, 1f)
    )
}

private fun IIOMetadata.getFrameRate(): Int {
    val root = getAsTree(nativeMetadataFormatName) as? IIOMetadataNode ?: return 1

    val nNodes: Int = root.length
    for (j in 0 until nNodes) {
        val node = root.item(j)
        if (node.nodeName.equals("GraphicControlExtension", ignoreCase = true) || node.nodeName.equals(
                "fcTL",
                ignoreCase = true
            )
        ) {
            val item = node as IIOMetadataNode
            val delay = if (item.hasAttribute("delayTime")) item.getAttribute("delayTime")
                .toInt() else item.getAttribute("delay_den").toInt()
            if (delay == 0) return 1
            return if (item.hasAttribute("delayTime")) (100f / delay).toInt() else delay
        }
    }
    return 1
}