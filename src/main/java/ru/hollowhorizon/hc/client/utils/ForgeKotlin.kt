package ru.hollowhorizon.hc.client.utils

import com.mojang.blaze3d.vertex.PoseStack
import net.irisshaders.iris.api.v0.IrisApi
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
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.fml.util.thread.SidedThreadGroups
import net.minecraftforge.registries.IForgeRegistry
import ru.hollowhorizon.hc.client.screens.util.Anchor
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.CapabilityStorage
import java.io.InputStream
import kotlin.reflect.KClass


val mc: Minecraft @OnlyIn(Dist.CLIENT) get() = Minecraft.getInstance()


val isProduction get() = FMLLoader.isProduction()
val isLogicalClient get() = Thread.currentThread().threadGroup != SidedThreadGroups.SERVER
val isLogicalServer get() = !isLogicalClient
val isPhysicalClient get() = FMLEnvironment.dist.isClient
val isPhysicalServer get() = !isPhysicalClient

val hasShaders = ModList.get().isLoaded("oculus") || ModList.get().isLoaded("optifine")

val areShadersEnabled = hasShaders && IrisApi.getInstance().config.areShadersEnabled()

operator fun <T : CapabilityInstance> ICapabilityProviderImpl<*>.get(capability: KClass<T>): T =
    getCapability(CapabilityStorage.getCapability(capability.java))
        .orElseThrow { IllegalStateException("Capability ${capability.simpleName} not found!") }

val String.rl get() = ResourceLocation(this)

@OnlyIn(Dist.CLIENT)
fun ResourceLocation.toIS(): InputStream {
    return HollowJavaUtils.getResource(this)
}

fun ResourceLocation.exists(): Boolean {
    return mc.resourceManager.getResource(this).isPresent
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
fun ResourceLocation.toTexture(): AbstractTexture = mc.textureManager.getTexture(this)

@OnlyIn(Dist.CLIENT)
fun AbstractTexture.render(stack: PoseStack, x: Int, y: Int, width: Int, height: Int) {
    this.bind()
    blit(stack, x, y, 0F, 0F, width, height, width, height)
}

@OnlyIn(Dist.CLIENT)
@JvmOverloads
fun Font.drawScaled(stack: PoseStack, anchor: Anchor = Anchor.CENTER, text: Component, x: Int, y: Int, color: Int, scale: Float) {
    stack.pushPose()
    stack.translate((x).toDouble(), (y).toDouble(), 0.0)
    stack.scale(scale, scale, 0F)
    when (anchor) {
        Anchor.CENTER -> this.draw(stack, text, -this.width(text) / 2f, -this.lineHeight / 2f, color)
        Anchor.END -> this.draw(stack, text, -this.width(text).toFloat(), 0f, color)
        Anchor.START -> this.draw(stack, text, 0f, 0f, color)
    }
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
inline fun PoseStack.use(usable: PoseStack.() -> Unit) {
    this.pushPose()
    usable()
    this.popPose()
}