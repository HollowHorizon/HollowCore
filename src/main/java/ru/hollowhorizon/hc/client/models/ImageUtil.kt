package ru.hollowhorizon.hc.client.models

import java.awt.Graphics2D
import java.awt.Image
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

object ImageUtil {
    fun createFlipped(image: BufferedImage): BufferedImage {
        val at = AffineTransform()
        at.concatenate(AffineTransform.getScaleInstance(1.0, -1.0))
        at.concatenate(AffineTransform.getTranslateInstance(0.0, -image.height.toDouble()))
        return createTransformed(image, at)
    }

    fun createTransformed(image: BufferedImage, at: AffineTransform?): BufferedImage {
        val newImage = BufferedImage(image.width, image.height, BufferedImage.TYPE_INT_ARGB)
        val g: Graphics2D = newImage.createGraphics()
        g.transform(at)
        g.drawImage(image, 0, 0, null)
        g.dispose()
        return newImage
    }

    @Throws(IOException::class)
    fun bufferedImageFromFile(file: File): BufferedImage {
        val image: Image = ImageIO.read(file)
        val bimage = BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        val bGr = bimage.createGraphics()
        bGr.drawImage(image, 0, 0, null)
        bGr.dispose()

        return bimage
    }
}