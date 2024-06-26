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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.client.server.IntegratedServer
import net.minecraft.core.RegistryAccess
import net.minecraft.network.chat.ClickEvent
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.HoverEvent
import net.minecraft.network.chat.MutableComponent
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.util.FormattedCharSequence
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.api.ICapabilityDispatcher
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import java.io.InputStream
import kotlin.reflect.KClass


val mc: Minecraft get() = Minecraft.getInstance()

enum class Axis(val x: Float, val y: Float, val z: Float) {
    X(1f, 0f, 0f),
    Y(0f, 1f, 0f),
    Z(0f, 0f, 1f);
}

val isProduction get() = isProduction_()
val isLogicalClient get() = isPhysicalClient && RenderSystem.isOnRenderThread()
val isLogicalServer get() = !isLogicalClient
val isPhysicalClient get() = !isPhysicalClient_()
val isPhysicalServer get() = !isPhysicalClient

val hasShaders get() = isModLoaded("oculus") || isModLoaded("iris") || isModLoaded("optifine")

val areShadersEnabled get() = hasShaders && areShadersEnabled_()

lateinit var isProduction_: () -> Boolean
lateinit var isPhysicalClient_: () -> Boolean
lateinit var areShadersEnabled_: () -> Boolean
lateinit var isModLoaded: (modid: String) -> Boolean
lateinit var currentServer: MinecraftServer

val registryAccess: RegistryAccess
    get() = if (currentServer is IntegratedServer) Minecraft.getInstance().connection!!.registryAccess()
    else currentServer.registryAccess()


fun fromJava(clazz: Class<*>) = clazz.kotlin

operator fun <O, T : CapabilityInstance> O.get(capability: KClass<T>): T = get(capability.java)

operator fun <O, T : CapabilityInstance> O.get(capability: Class<T>): T =
    (this as ICapabilityDispatcher).capabilities.first { it.javaClass == capability } as T

val String.rl get() = ResourceLocation.parse(this)

fun resource(resource: String) = ResourceLocation.fromNamespaceAndPath(HollowCore.MODID, resource).stream

fun ResourceLocation.toIS(): InputStream {
    return HollowJavaUtils.getResource(this)
}

fun ResourceLocation.exists(): Boolean {
    return mc.resourceManager.getResource(this).isPresent
}

val ResourceLocation.stream: InputStream
    get() = HollowJavaUtils.getResource(this)

val PLACEHOLDER: MutableComponent get() = Component.empty()
val String.mcText: MutableComponent get() = Component.literal(this)
val String.mcTranslate: MutableComponent get() = Component.translatable(this)
fun String.mcTranslate(vararg args: Any) = Component.translatable(this, *args)

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

fun Screen.open() {
    mc.setScreen(this)
}

fun ResourceLocation.toTexture(): AbstractTexture = mc.textureManager.getTexture(this)

fun ResourceLocation.render(g: GuiGraphics, x: Int, y: Int, width: Int, height: Int) {

    g.blit(this, x, y, 0F, 0F, width, height, width, height)
}

enum class Anchor {
    LEFT, CENTER, RIGHT
}

@JvmOverloads
fun GuiGraphics.drawScaled(
    anchor: Anchor = Anchor.CENTER,
    text: FormattedCharSequence,
    x: Int,
    y: Int,
    color: Int = 0xFFFFFF,
    scale: Float = 1.0f,
    shadow: Boolean = true,
) {
    val font = Minecraft.getInstance().font
    pose().popPose()
    pose().translate((x).toDouble(), (y).toDouble(), 0.0)
    pose().scale(scale, scale, 0F)
    when (anchor) {
        Anchor.CENTER -> drawString(font, text, -font.width(text) / 2, -font.lineHeight / 2, color, shadow)
        Anchor.RIGHT -> drawString(font, text, -font.width(text), 0, color, shadow)
        Anchor.LEFT -> drawString(font, text, 0, 0, color, shadow)
    }
    pose().popPose()
}

@JvmOverloads
fun GuiGraphics.drawScaled(
    anchor: Anchor = Anchor.CENTER,
    text: Component,
    x: Int,
    y: Int,
    color: Int = 0xFFFFFF,
    scale: Float = 1.0f,
    shadow: Boolean = true,
) {
    val font = Minecraft.getInstance().font
    pose().popPose()
    pose().translate((x).toDouble(), (y).toDouble(), 0.0)
    pose().scale(scale, scale, 0F)
    when (anchor) {
        Anchor.CENTER -> drawString(font, text, -font.width(text) / 2, -font.lineHeight / 2, color, shadow)
        Anchor.RIGHT -> drawString(font, text, -font.width(text), 0, color, shadow)
        Anchor.LEFT -> drawString(font, text, 0, 0, color, shadow)
    }
    pose().popPose()
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