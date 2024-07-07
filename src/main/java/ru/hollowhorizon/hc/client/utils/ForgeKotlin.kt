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

package ru.hollowhorizon.hc.client.utils

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import dev.ftb.mods.ftbteams.data.TeamBase
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.coderbot.iris.Iris
import net.coderbot.iris.pipeline.newshader.CoreWorldRenderingPipeline
import net.irisshaders.iris.api.v0.IrisApi
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Font
import net.minecraft.client.gui.GuiComponent.blit
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.common.capabilities.ICapabilityProvider
import net.minecraftforge.fml.ModList
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.fml.util.thread.SidedThreadGroups
import net.minecraftforge.registries.IForgeRegistry
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.CapabilityStorage
import ru.hollowhorizon.hc.common.ui.Anchor
import java.io.InputStream
import kotlin.reflect.KClass


val mc: Minecraft @OnlyIn(Dist.CLIENT) get() = Minecraft.getInstance()


val isProduction get() = FMLLoader.isProduction()
val isLogicalClient get() = Thread.currentThread().threadGroup != SidedThreadGroups.SERVER
val isLogicalServer get() = !isLogicalClient
val isPhysicalClient get() = FMLEnvironment.dist.isClient
val isPhysicalServer get() = !isPhysicalClient

val hasShaders get() = ModList.get().isLoaded("oculus") || ModList.get().isLoaded("optifine")

val areShadersEnabled get() = hasShaders && isDeferred()

fun isDeferred() =
    (Iris.getPipelineManager().pipelineNullable as? CoreWorldRenderingPipeline)?.shouldOverrideShaders() == true

fun fromJava(clazz: Class<*>) = clazz.kotlin

operator fun <T : CapabilityInstance> ICapabilityProvider.get(capability: KClass<T>): T =
    getCapability(CapabilityStorage.getCapability(capability.java))
        .orElseThrow { IllegalStateException("Capability ${capability.simpleName} not found!") } as T

operator fun <T : CapabilityInstance> ICapabilityProvider.get(capability: Class<T>) = get(capability.kotlin)

fun <T : CapabilityInstance> TeamBase.capability(capability: KClass<T>) = (this as ICapabilityProvider)[capability]

val String.rl get() = ResourceLocation(this)

fun resource(resource: String) = ResourceLocation(HollowCore.MODID, resource).stream

@OnlyIn(Dist.CLIENT)
fun ResourceLocation.toIS(): InputStream {
    return HollowJavaUtils.getResource(this)
}

fun ResourceLocation.exists(): Boolean {
    return mc.resourceManager.getResource(this).isPresent
}

val ResourceLocation.stream: InputStream
    get() = HollowJavaUtils.getResource(this)

val String.mcText: MutableComponent
    get() = Component.literal(this)

val String.mcTranslate: MutableComponent
    get() = Component.translatable(this)

fun String.mcTranslate(vararg args: Any): MutableComponent {
    return Component.translatable(this, *args)
}

operator fun MutableComponent.plus(other: Component): MutableComponent = this.copy().append(other)

fun MutableComponent.colored(color: Int): MutableComponent = this.withStyle { it.withColor(color) }
fun MutableComponent.bold(): MutableComponent = this.withStyle { it.withBold(true) }
fun MutableComponent.italic(): MutableComponent = this.withStyle { it.withItalic(true) }
fun MutableComponent.obfuscated(): MutableComponent = this.withStyle { it.withObfuscated(true) }
fun MutableComponent.underlined(): MutableComponent = this.withStyle { it.withUnderlined(true) }
fun MutableComponent.strikethrough(): MutableComponent = this.withStyle { it.withStrikethrough(true) }
fun MutableComponent.font(font: ResourceLocation) = this.withStyle { it.withFont(font) }
fun MutableComponent.onClickUrl(url: String): MutableComponent =
    this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url)) }

fun MutableComponent.onClickCommand(command: String): MutableComponent =
    this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.RUN_COMMAND, command)) }

fun MutableComponent.onClickSuggestion(command: String): MutableComponent =
    this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command)) }

fun MutableComponent.onClickCopy(text: String): MutableComponent =
    this.withStyle { it.withClickEvent(ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text)) }

fun MutableComponent.onHoverText(text: Component): MutableComponent =
    this.withStyle { it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, text)) }

fun MutableComponent.onHoverText(text: String) = onHoverText(Component.literal(text))
fun MutableComponent.onHoverItem(item: ItemStack) =
    this.withStyle { it.withHoverEvent(HoverEvent(HoverEvent.Action.SHOW_ITEM, HoverEvent.ItemStackInfo(item))) }

fun MutableComponent.onHoverEntity(entity: Entity) = this.withStyle {
    it.withHoverEvent(
        HoverEvent(
            HoverEvent.Action.SHOW_ENTITY,
            HoverEvent.EntityTooltipInfo(entity.type, entity.uuid, entity.name)
        )
    )
}

@OnlyIn(Dist.CLIENT)
fun Screen.open() {
    mc.setScreen(this)
}

@OnlyIn(Dist.CLIENT)
fun ResourceLocation.toTexture(): AbstractTexture = mc.textureManager.getTexture(this)

@OnlyIn(Dist.CLIENT)
fun AbstractTexture.render(stack: PoseStack, x: Int, y: Int, width: Int, height: Int) {
    RenderSystem.bindTexture(this.id)
    blit(stack, x, y, 0F, 0F, width, height, width, height)
}

@OnlyIn(Dist.CLIENT)
@JvmOverloads
fun Font.drawScaled(
    stack: PoseStack,
    anchor: Anchor = Anchor.CENTER,
    text: FormattedCharSequence,
    x: Int,
    y: Int,
    color: Int = 0xFFFFFF,
    scale: Float = 1.0f,
    shadow: Boolean = true,
) {
    val drawMethod: (PoseStack, FormattedCharSequence, Float, Float, Int) -> Unit =
        if (!shadow) this::draw else this::drawShadow

    stack.pushPose()
    stack.translate((x).toDouble(), (y).toDouble(), 0.0)
    stack.scale(scale, scale, 0F)
    when (anchor) {
        Anchor.CENTER -> drawMethod(stack, text, -this.width(text) / 2f, -this.lineHeight / 2f, color)
        Anchor.END -> drawMethod(stack, text, -this.width(text).toFloat(), 0f, color)
        Anchor.START -> drawMethod(stack, text, 0f, 0f, color)
    }
    stack.popPose()
}

@OnlyIn(Dist.CLIENT)
@JvmOverloads
fun Font.drawScaled(
    stack: PoseStack,
    anchor: Anchor = Anchor.CENTER,
    text: Component,
    x: Int,
    y: Int,
    color: Int = 0xFFFFFF,
    scale: Float = 1.0f,
    shadow: Boolean = true,
) {
    val drawMethod: (PoseStack, Component, Float, Float, Int) -> Unit = if (!shadow) this::draw else this::drawShadow

    stack.pushPose()
    stack.translate((x).toDouble(), (y).toDouble(), 0.0)
    stack.scale(scale, scale, 0F)
    when (anchor) {
        Anchor.CENTER -> drawMethod(stack, text, -this.width(text) / 2f, -this.lineHeight / 2f, color)
        Anchor.END -> drawMethod(stack, text, -this.width(text).toFloat(), -this.lineHeight / 2f, color)
        Anchor.START -> drawMethod(stack, text, 0f, -this.lineHeight / 2f, color)
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

data class RGBA(val r: Float, val g: Float, val b: Float, val a: Float) {
    fun toInt(): Int {
        val red = (r * 255.0f + 0.5f).toInt() shl 16
        val green = (g * 255.0f + 0.5f).toInt() shl 8
        val blue = (b * 255.0f + 0.5f).toInt()
        val alpha = (a * 255.0f + 0.5f).toInt() shl 24
        return alpha or red or green or blue
    }
}

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

fun <A, B> ((A) -> B).memoize(): (A) -> B {
    val cache: MutableMap<A, B> = Object2ObjectOpenHashMap()
    return {
        cache.getOrPut(it) { this(it) }
    }
}