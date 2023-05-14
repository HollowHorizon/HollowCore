package ru.hollowhorizon.hc.client.render.font

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.resources.IResource
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.TextFormatting
import org.lwjgl.opengl.GL11
import java.awt.Font
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.InputStream

class TrueTypeFont(private var font: Font, var scale: Float = 1.0f) {
    private val usedFonts = ArrayList<Font>()
    private val textcache = LRUHashMap<String, GlyphCache>(100)
    private val glyphcache = HashMap<Char, Glyph>()
    private val textures = ArrayList<TextureCache>()
    private var lineHeight = 1
    private val globalG = BufferedImage(1, 1, 2).graphics as Graphics2D
    private var specialChar = 167

    init {
        globalG.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        lineHeight = globalG.getFontMetrics(font).height
    }

    constructor(resource: ResourceLocation, fontSize: Int, scale: Float) : this(with(Minecraft.getInstance()) {
        val r: IResource = Minecraft.getInstance().resourceManager.getResource(resource)

        val stream: InputStream = r.inputStream
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val font = Font.createFont(0, stream)
        environment.registerFont(font)
        return@with font.deriveFont(0, fontSize.toFloat())
    }, scale)

    fun setSpecial(c: Char) {
        specialChar = c.code
    }

    @Suppress("DEPRECATION")
    fun draw(text: String, x: Float, y: Float, color: Int) {
        val cache = getOrCreateCache(text)
        val r = (color shr 16 and 255).toFloat() / 255.0f
        val g = (color shr 8 and 255).toFloat() / 255.0f
        val b = (color and 255).toFloat() / 255.0f
        RenderSystem.color4f(r, g, b, 1.0f)
        RenderSystem.enableBlend()
        RenderSystem.pushMatrix()
        RenderSystem.translatef(x, y, 0.0f)
        RenderSystem.scalef(scale, scale, 1.0f)
        var i = 0.0f
        val var10: Iterator<*> = cache.glyphs.iterator()
        while (var10.hasNext()) {
            val gl: Glyph = var10.next() as Glyph
            if (gl.type != GlyphType.NORMAL) {
                if (gl.type == GlyphType.RESET) {
                    RenderSystem.color4f(r, g, b, 1.0f)
                } else if (gl.type == GlyphType.COLOR) {
                    RenderSystem.color4f(
                        (gl.color shr 16 and 255).toFloat() / 255.0f,
                        (gl.color shr 8 and 255).toFloat() / 255.0f,
                        (gl.color and 255).toFloat() / 255.0f,
                        1.0f
                    )
                }
            } else {
                RenderSystem.bindTexture(gl.texture)
                fillGradient(
                    i,
                    0.0f,
                    gl.x.toFloat() * textureScale(),
                    gl.y.toFloat() * textureScale(),
                    gl.width.toFloat() * textureScale(),
                    gl.height.toFloat() * textureScale()
                )
                i += gl.width.toFloat() * textureScale()
            }
        }
        RenderSystem.disableBlend()
        RenderSystem.popMatrix()
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f)
    }

    private fun getOrCreateCache(text: String): GlyphCache {
        return textcache.computeIfAbsent(text) { _ ->
            val cache = GlyphCache(this)

            var i = 0
            while (i < text.length) {
                val c = text[i]
                if (c.code == specialChar && i + 1 < text.length) {
                    val next = text.lowercase()[i + 1]
                    val index = "0123456789abcdefklmnor".indexOf(next)

                    if (index >= 0) {
                        val g = Glyph(this)
                        if (index < 16) {
                            g.type = GlyphType.COLOR
                            g.color = TextFormatting.getByCode(next)?.color ?: 0
                        } else if (index == 16) {
                            g.type = GlyphType.RANDOM
                        } else if (index == 17) {
                            g.type = GlyphType.BOLD
                        } else if (index == 18) {
                            g.type = GlyphType.STRIKETHROUGH
                        } else if (index == 19) {
                            g.type = GlyphType.UNDERLINE
                        } else if (index == 20) {
                            g.type = GlyphType.ITALIC
                        } else {
                            g.type = GlyphType.RESET
                        }
                        cache.glyphs.add(g)
                        i++
                        continue
                    }
                }
                val g: Glyph = getOrCreateGlyph(c)
                cache.glyphs.add(g)
                cache.width += g.width
                cache.height = cache.height.coerceAtLeast(g.height)
                i++
            }
            return@computeIfAbsent cache
        }
    }

    private fun getOrCreateGlyph(c: Char): Glyph {
        var g: Glyph? = glyphcache[c]
        return if (g != null) {
            g
        } else {
            var cache = currentTexture
            val font = getFontForChar(c)
            val metrics = globalG.getFontMetrics(font)
            g = Glyph(this)
            g.width = metrics.charWidth(c).coerceAtLeast(1)
            g.height = metrics.height.coerceAtLeast(1)
            if (cache.x + g.width >= 512) {
                cache.x = 0
                cache.y += lineHeight + 1
                if (cache.y >= 512) {
                    cache.full = true
                    cache = currentTexture
                }
            }
            g.x = cache.x
            g.y = cache.y
            cache.x += g.width + 3
            lineHeight = lineHeight.coerceAtLeast(g.height)
            cache.g.font = font
            cache.g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            cache.g.drawString(c.toString() + "", g.x, g.y + metrics.ascent)
            g.texture = cache.textureId
            TextureUtil.uploadTextureImage(cache.textureId, cache.bufferedImage)
            glyphcache[c] = g
            g
        }
    }


    private val currentTexture: TextureCache
        get() = textures.firstOrNull { !it.full } ?: TextureCache(this).also { textures.add(it) }

    fun drawCentered(text: String, x: Float, y: Float, color: Int) {
        draw(text, x - width(text).toFloat() / 2.0f, y, color)
    }

    private fun getFontForChar(c: Char): Font {
        return if (font.canDisplay(c)) {
            font
        } else {
            usedFonts.firstOrNull { it.canDisplay(c) } ?: run {
                val fa = Font("Arial Unicode MS", 0, font.size)
                return@run if (fa.canDisplay(c)) fa else null
            } ?: allFonts.first { !it.canDisplay(c) }.also { usedFonts.add(it.deriveFont(0, font.size.toFloat())) }
        }
    }

    fun fillGradient(x: Float, y: Float, textureX: Float, textureY: Float, width: Float, height: Float) {
        val f = 0.00390625f
        val f1 = 0.00390625f
        val zLevel = 0
        with(Tessellator.getInstance().builder) {
            begin(7, DefaultVertexFormats.POSITION_TEX)
            vertex(x.toDouble(), (y + height).toDouble(), zLevel.toDouble()).uv(textureX * f, (textureY + height) * f1)
                .endVertex()
            vertex((x + width).toDouble(), (y + height).toDouble(), zLevel.toDouble()).uv((textureX + width) * f, (textureY + height) * f1).endVertex()
            vertex((x + width).toDouble(), y.toDouble(), zLevel.toDouble()).uv((textureX + width) * f, textureY * f1).endVertex()
            vertex(x.toDouble(), y.toDouble(), zLevel.toDouble()).uv(textureX * f, textureY * f1).endVertex()
        }
        Tessellator.getInstance().end()
    }

    fun width(text: String): Int {
        val cache: GlyphCache = getOrCreateCache(text)
        return (cache.width.toFloat() * scale * textureScale()).toInt()
    }

    fun height(text: String?): Int {
        return if (text != null && !text.trim { it <= ' ' }.isEmpty()) {
            val cache: GlyphCache = getOrCreateCache(text)
            1.coerceAtLeast((cache.height.toFloat() * scale * textureScale()).toInt())
        } else {
            (lineHeight.toFloat() * scale * textureScale()).toInt()
        }
    }

    private fun textureScale(): Float {
        return 0.5f
    }

    fun dispose() {
        val var1: Iterator<*> = textures.iterator()
        while (var1.hasNext()) {
            val cache: TextureCache = var1.next() as TextureCache
            RenderSystem.deleteTexture(cache.textureId)
        }
        textcache.clear()
    }

    val fontName: String
        get() = font.fontName

    companion object {
        private const val MaxWidth = 512
        private val allFonts = listOf(*GraphicsEnvironment.getLocalGraphicsEnvironment().allFonts)

        class GlyphCache(val font: TrueTypeFont) {
            var width = 0
            var height = 0
            var glyphs = ArrayList<Glyph>()
        }

        class Glyph(val font: TrueTypeFont) {
            var type = GlyphType.NORMAL
            var color = -1
            var x = 0
            var y = 0
            var height = 0
            var width = 0
            var texture = 0
        }

        enum class GlyphType {
            NORMAL, COLOR, RANDOM, BOLD, STRIKETHROUGH, UNDERLINE, ITALIC, RESET, OTHER
        }

        class TextureCache(val font: TrueTypeFont) {
            var x = 0
            var y = 0
            var textureId = GL11.glGenTextures()
            var bufferedImage = BufferedImage(512, 512, 2)
            var g = bufferedImage.graphics as Graphics2D
            var full = false
        }
    }
}

class LRUHashMap<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(maxSize, 0.75f, true) {
    override fun removeEldestEntry(eldest: Map.Entry<K, V>) = this.size > maxSize
}
