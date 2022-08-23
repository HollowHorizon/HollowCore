package ru.hollowhorizon.hc.client.utils

import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.AbstractGui
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

@OnlyIn(Dist.CLIENT)
val player = mc.player!!

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