package ru.hollowhorizon.hc.client.render.particles

import com.mojang.blaze3d.platform.GlStateManager
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.particle.ParticleEngine.MutableSpriteSet
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.texture.TextureAtlas
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.util.FastColor

import net.minecraft.util.Mth
import org.lwjgl.opengl.GL11
import java.awt.Color


class HollowParticle(
    level: ClientLevel,
    private val options: HollowParticleOptions,
    private val spriteSet: SpriteSet,
    x: Double, y: Double, z: Double,
    mX: Double, mY: Double, mZ: Double
) : TextureSheetParticle(level, x, y, z, mX, mY, mZ) {
    private var reachedPositiveAlpha = false
    private var reachedPositiveScale = false
    private var hsv1 = FloatArray(3)
    private var hsv2 = FloatArray(3)

    init {
        options.apply {
            Color.RGBtoHSB(
                (255 * 1.0f.coerceAtMost(colorData.r1)).toInt(),
                (255 * 1.0f.coerceAtMost(colorData.g1)).toInt(),
                (255 * 1.0f.coerceAtMost(colorData.b1)).toInt(),
                hsv1
            )
            Color.RGBtoHSB(
                (255 * 1.0f.coerceAtMost(colorData.r2)).toInt(),
                (255 * 1.0f.coerceAtMost(colorData.g2)).toInt(),
                (255 * 1.0f.coerceAtMost(colorData.b2)).toInt(),
                hsv2
            )

            when (spritePicker) {
                SpritePicker.RANDOM_SPRITE -> pickSprite(spriteSet)
                SpritePicker.FIRST_INDEX, SpritePicker.WITH_AGE -> pickSprite(0)
                SpritePicker.LAST_INDEX -> pickSprite(-1)
            }

            hasPhysics = !noClip
            this@HollowParticle.gravity = gravity
            this@HollowParticle.lifetime = lifetime
        }
        xd = mX
        yd = mY
        zd = mZ

        tick()
    }

    override fun tick() {
        super.tick()

        options.apply {
            var shouldAttemptRemoval = discardType == DiscardType.INVISIBLE
            if (
                discardType == DiscardType.ENDING_CURVE_INVISIBLE &&
                scaleData.getProgress(age, lifetime) > 0.5f || transparencyData.getProgress(age, lifetime) > 0.5f
            ) shouldAttemptRemoval = true

            if (shouldAttemptRemoval) {
                if (reachedPositiveAlpha && alpha <= 0 || reachedPositiveScale && quadSize <= 0) {
                    remove()
                    return
                }
            }

            if (spritePicker == SpritePicker.WITH_AGE) {
                setSpriteFromAge(spriteSet)
            }
            pickColor(colorData.colorCurveEasing(colorData.getProgress(age, lifetime)))

            quadSize = scaleData.getValue(age, lifetime)
            alpha = transparencyData.getValue(age, lifetime)
            oRoll = roll
            roll += spinData.getValue(age, lifetime)
        }

        if (!reachedPositiveAlpha && alpha > 0) reachedPositiveAlpha = true
        if (!reachedPositiveScale && quadSize > 0) reachedPositiveScale = true
    }

    private fun pickColor(colorCoeff: Float) {
        val h = Mth.rotLerp(colorCoeff, 360f * hsv1[0], 360f * hsv2[0]) / 360f
        val s = Mth.lerp(colorCoeff, hsv1[1], hsv2[1])
        val v = Mth.lerp(colorCoeff, hsv1[2], hsv2[2])
        val packed: Int = Color.HSBtoRGB(h, s, v)
        val r = FastColor.ARGB32.red(packed) / 255.0f
        val g = FastColor.ARGB32.green(packed) / 255.0f
        val b = FastColor.ARGB32.blue(packed) / 255.0f
        setColor(r, g, b)
    }

    private fun pickSprite(spriteIndex: Int) {
        val set = spriteSet as? MutableSpriteSet ?: return

        if (spriteIndex == -1) setSprite(set.sprites.last())
        if (spriteIndex < set.sprites.size && spriteIndex >= 0) setSprite(set.sprites[spriteIndex])
    }

    override fun getRenderType() = HollowRenderType
}

object HollowRenderType: ParticleRenderType {
    override fun begin(bufferBuilder: BufferBuilder, textureManager: TextureManager) {
        RenderSystem.depthMask(false)
        //RenderSystem.disableDepthTest()
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_PARTICLES)
        RenderSystem.setShader(GameRenderer::getParticleShader)
        RenderSystem.enableBlend()
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)

        textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).setBlurMipmap(false, false)

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE)
    }

    override fun end(tessellator: Tesselator) {
        tessellator.end()
        RenderSystem.enableDepthTest()
        Minecraft.getInstance().textureManager.getTexture(TextureAtlas.LOCATION_PARTICLES).restoreLastBlurMipmap()

        RenderSystem.disableBlend()
        RenderSystem.depthMask(true)
    }

}