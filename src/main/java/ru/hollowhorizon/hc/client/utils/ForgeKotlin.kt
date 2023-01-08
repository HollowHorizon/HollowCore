package ru.hollowhorizon.hc.client.utils

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.AbstractGui
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.renderer.texture.Texture
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.vector.Vector3f
import net.minecraft.util.text.ITextComponent
import net.minecraft.util.text.StringTextComponent
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.HollowCore
import java.io.InputStream

@OnlyIn(Dist.CLIENT)
val mc = Minecraft.getInstance()

operator fun Vector3f.plusAssign(vector: Vector3f) {
    this.add(vector)
}

fun String.toRL(): ResourceLocation {
    return ResourceLocation(this)
}

@OnlyIn(Dist.CLIENT)
fun ResourceLocation.toIS(): InputStream {
    return HollowJavaUtils.getResource(this)
}

fun String.toSTC(): ITextComponent {
    return StringTextComponent(this)
}

fun String.toTTC(): ITextComponent {
    return TranslationTextComponent(this)
}

fun Screen.open() {
    mc.setScreen(this)
}

fun ResourceLocation.toTexture(): Texture {
    val texture: Texture? = mc.textureManager.getTexture(this)
    return if (texture == null) {
        HollowCore.LOGGER.warn("Texture \"$this\" not found")
        mc.textureManager.getTexture("textures/block/beacon.png".toRL())!!
    } else texture
}

fun Texture.render(stack: MatrixStack, x: Int, y: Int, width: Int, height: Int) {
    this.bind()
    AbstractGui.blit(stack, x, y, 0F, 0F, width, height, width, height)
}

fun FontRenderer.drawScaled(stack: MatrixStack, text: ITextComponent, x: Int, y: Int, color: Int, scale: Float) {
    stack.pushPose()
    stack.translate((x).toDouble(), (y).toDouble(), 0.0)
    stack.scale(scale, scale, 0F)
    this.draw(stack, text, 0f, 0f, color)
    stack.popPose()
}

fun FontRenderer.drawScaledEnd(stack: MatrixStack, text: ITextComponent, x: Int, y: Int, color: Int, scale: Float) {
    stack.pushPose()
    stack.translate((x).toDouble(), (y).toDouble(), 0.0)
    stack.scale(scale, scale, 0F)
    this.draw(stack, text, -this.width(text) + 0f, 0f, color)
    stack.popPose()
}

fun FontRenderer.drawCentredScaled(stack: MatrixStack, text: ITextComponent, x: Int, y: Int, color: Int, scale: Float) {
    stack.pushPose()
    stack.translate((x).toDouble(), (y).toDouble(), 0.0)
    stack.scale(scale, scale, 0F)
    this.draw(stack, text, -this.width(text) / 2f, -this.lineHeight / 2f, color)
    stack.popPose()
}

fun Int.toRGBA(): RGBA {
    return RGBA(
        (this shr 16 and 255).toFloat() / 255.0f,
        (this shr 8 and 255).toFloat() / 255.0f,
        (this and 255).toFloat() / 255.0f,
        (this shr 24 and 255).toFloat() / 255.0f
    )
}

data class RGBA(val r: Float, val g: Float, val b: Float, val a: Float)

