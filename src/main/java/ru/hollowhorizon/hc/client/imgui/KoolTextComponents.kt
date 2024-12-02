package ru.hollowhorizon.hc.client.imgui

import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.util.Color
import de.fabmax.kool.util.MsdfFont
import net.minecraft.locale.Language
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.contents.LiteralContents
import net.minecraft.network.chat.contents.TranslatableContents

fun UiNode.Component(
    component: Component,
    block: TextAreaScope.() -> Unit,
) {
    TextArea(
        ListTextLineProvider(mutableListOf(TextLine(component.coloredText()))),
        hScrollbarModifier = { it.margin(start = sizes.gap, end = sizes.gap * 2f, bottom = sizes.gap) },
        vScrollbarModifier = { it.margin(sizes.gap) },
        block=block
    )
}

private fun Component.coloredText(): MutableList<Pair<String, TextAttributes>> {
    return siblings.flatMap {
        val result = mutableListOf<Pair<String, TextAttributes>>()
        result += it.attributes()
        result += it.coloredText()
        result
    }.toMutableList()
}

private fun Component.attributes(): Pair<String, TextAttributes> {

    val color = style.color?.value ?: 0xFFFFFF
    val isUnderlined = style.isUnderlined
    val isStrikethrough = style.isStrikethrough
    val isObfuscated = style.isObfuscated

    val red = (color shr 16 and 0xFF).toFloat() / 255.0f
    val green = (color shr 8 and 0xFF).toFloat() / 255.0f
    val blue = (color and 0xFF).toFloat() / 255.0f

    var text = when (val content = contents) {
        is LiteralContents -> content.text
        is TranslatableContents -> {
            String.format(
                Language.getInstance().getOrDefault(content.key),
                *content.args.map { if (it is Component) it.string else it }.toTypedArray()
            )
        }

        Component.EMPTY -> "\n"
        else -> error("Unknown text component")
    }

    if (isObfuscated) text = obfuscatedString(text.length)

    return text to TextAttributes(MsdfFont.DEFAULT_FONT, Color(red, green, blue))
}

private fun obfuscatedString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + ('А'..'Я') + ('а'..'я')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}