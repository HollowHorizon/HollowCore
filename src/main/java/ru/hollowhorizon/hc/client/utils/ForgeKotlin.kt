package ru.hollowhorizon.hc.client.utils

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiComponent.blit
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.util.thread.SidedThreadGroups
import net.minecraftforge.registries.IForgeRegistry
import ru.hollowhorizon.hc.HollowCore
import java.io.InputStream


val mc: Minecraft
    @OnlyIn(Dist.CLIENT)
    get() = Minecraft.getInstance()


val isLogicalClient: Boolean
    get() = Thread.currentThread().threadGroup != SidedThreadGroups.SERVER
val isLogicalServer: Boolean
    get() = !isLogicalClient
val isPhysicalClient: Boolean
    get() = FMLEnvironment.dist.isClient
val isPhysicalServer: Boolean
    get() = !isPhysicalClient
val isIdeMode: Boolean
    get() = false

fun String.toRL(): ResourceLocation {
    return ResourceLocation(this)
}

val String.rl: ResourceLocation
    get() = ResourceLocation(this)

@OnlyIn(Dist.CLIENT)
fun ResourceLocation.toIS(): InputStream {
    return HollowJavaUtils.getResource(this)
}

val ResourceLocation.stream: InputStream
    get() = HollowJavaUtils.getResource(this)

fun String.toSTC(): Component {
    return Component.literal(this)
}

val String.mcText: Component
    get() = Component.literal(this)

fun String.toTTC(): Component {
    return Component.translatable(this)
}

val String.mcTranslate: Component
    get() = Component.translatable(this)

@OnlyIn(Dist.CLIENT)
fun Screen.open() {
    mc.setScreen(this)
}

@OnlyIn(Dist.CLIENT)
fun ResourceLocation.toTexture(): AbstractTexture {
    val texture: AbstractTexture = mc.textureManager.getTexture(this)
    return if (texture == null) {
        HollowCore.LOGGER.warn("Texture \"$this\" not found")
        mc.textureManager.getTexture("textures/block/beacon.png".toRL())
    } else texture
}

@OnlyIn(Dist.CLIENT)
fun AbstractTexture.render(stack: PoseStack, x: Int, y: Int, width: Int, height: Int) {
    this.bind()
    blit(stack, x, y, 0F, 0F, width, height, width, height)
}

@OnlyIn(Dist.CLIENT)
fun Font.drawScaled(stack: PoseStack, text: Component, x: Int, y: Int, color: Int, scale: Float) {
    stack.pushPose()
    stack.translate((x).toDouble(), (y).toDouble(), 0.0)
    stack.scale(scale, scale, 0F)
    this.draw(stack, text, 0f, 0f, color)
    stack.popPose()
}

@OnlyIn(Dist.CLIENT)
fun Font.drawScaledEnd(stack: PoseStack, text: Component, x: Int, y: Int, color: Int, scale: Float) {
    stack.pushPose()
    stack.translate((x).toDouble(), (y).toDouble(), 0.0)
    stack.scale(scale, scale, 0F)
    this.draw(stack, text, -this.width(text) + 0f, 0f, color)
    stack.popPose()
}

@OnlyIn(Dist.CLIENT)
fun Font.drawCentredScaled(stack: PoseStack, text: Component, x: Int, y: Int, color: Int, scale: Float) {
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

fun <V> ResourceLocation.valueFrom(registry: IForgeRegistry<V>): V {
    return registry.getValue(this) ?: throw IllegalArgumentException("Value $this not found in registry $registry")
}

fun Item.stack(count: Int = 1, nbt: CompoundTag? = null): ItemStack {
    return ItemStack(this, count, nbt)
}

@OnlyIn(Dist.CLIENT)
fun PoseStack.use(usable: PoseStack.() -> Unit) {
    this.pushPose()
    usable()
    this.popPose()
}