package ru.hollowhorizon.hc.client.models

import jassimp.AiTextureType
import net.minecraft.client.renderer.texture.DynamicTexture
import net.minecraft.client.renderer.texture.Texture
import net.minecraft.client.renderer.texture.TextureUtil
import net.minecraft.resources.IResourceManager
import java.awt.image.BufferedImage
import java.nio.IntBuffer


class Texture(val id: Int, val type: AiTextureType, width: Int, height: Int) {
    val width: Int
    val height: Int

    init {
        this.width = width
        this.height = height
    }
}

class BufferedTexture(img: BufferedImage): Texture() {
    init {
        val width = img.width;
        val height = img.height;
        val textureData = IntArray(width * height)
        TextureUtil.prepareImage(this.getId(), width, height)
        img.getRGB(0, 0, width, height, textureData, 0, width)

        val buffer = IntBuffer.wrap(textureData)

        TextureUtil.initTexture(buffer, width, height)

    }

    override fun load(manager: IResourceManager) {

    }


}