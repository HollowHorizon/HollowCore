/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.textures

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.platform.NativeImage
import com.mojang.blaze3d.platform.TextureUtil
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.SimpleTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import org.apache.commons.io.IOUtils
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.handlers.TickHandler
import java.io.*
import java.util.*
import javax.imageio.ImageIO
import javax.imageio.ImageReadParam

@JvmField
val GIF_TEXTURES = HashMap<ResourceLocation, GifTexture>()

class GifTexture(location: ResourceLocation) : SimpleTexture(location) {
    val frames = HashMap<Float, NativeImage>()
    var keys: FloatArray = FloatArray(1) { 0f }
    var fullTime = 1f

    val Float.animIndex: Int
        get() {
            val index = Arrays.binarySearch(keys, this)

            return if (index >= 0) index
            else 0.coerceAtLeast(-index - 2)
        }

    override fun getId(): Int {
        val id = super.getId()
        val time = (TickHandler.currentTicks + Minecraft.getInstance().partialTick) % fullTime

        val frame = frames[keys[time.animIndex]]

        GlStateManager._bindTexture(id)
        frame?.upload(0, 0, 0, false)

        return id
    }

    override fun load(pResourceManager: ResourceManager) {
        try {
            val data: ByteArray = IOUtils.toByteArray(pResourceManager.getResource(location).get().open())
            val type = readType(ByteArrayInputStream(data))
            if (type.equals("gif", ignoreCase = true)) {
                    val gifDecoder = GifDecoder()
                    val status = gifDecoder.read(BufferedInputStream(ByteArrayInputStream(data)))
                    if (status == 0) {
                        keys = FloatArray(gifDecoder.frameCount)
                        keys[0] = 0f

                        var stream = ByteArrayOutputStream()
                        ImageIO.write(gifDecoder.getFrame(0), "png", stream)
                        stream.flush()
                        this.frames[0f] = NativeImage.read(ByteArrayInputStream(stream.toByteArray()))


                        for (i in 1 until gifDecoder.frameCount) {
                            stream = ByteArrayOutputStream()
                            ImageIO.write(gifDecoder.getFrame(i), "png", stream)
                            stream.flush()
                            val time = gifDecoder.getDelay(i) / 50f //перевод в тики
                            keys[i] = keys[i - 1] + time
                            this.frames[keys[i]] = NativeImage.read(ByteArrayInputStream(stream.toByteArray()))

                            stream.close()
                        }
                        fullTime = frames.keys.last()

                        TextureUtil.prepareImage(super.getId(), frames[0f]!!.width, frames[0f]!!.height)
                    }
            }

            //nativeimage = NativeImage.read(ByteArrayInputStream(data))
        } catch (var5: IOException) {
            HollowCore.LOGGER.warn("Error while loading the texture", var5)
        }
    }

    @Throws(IOException::class)
    private fun readType(input: InputStream): String {
        input.mark(0)
        val stream = ImageIO.createImageInputStream(input)
        val imageReaders = ImageIO.getImageReaders(stream)
        if (!imageReaders.hasNext()) {
            return ""
        } else {
            val reader = imageReaders.next()
            if (reader.formatName.equals("gif", ignoreCase = true)) {
                return "gif"
            } else {
                val param: ImageReadParam = reader.defaultReadParam
                reader.setInput(stream, true, true)

                try {
                    reader.read(0, param)
                } catch (var9: IOException) {
                    HollowCore.LOGGER.error("Failed to parse input format", var9)
                } finally {
                    reader.dispose()
                    stream.close()
                }

                input.reset()
                return reader.formatName
            }
        }
    }
}