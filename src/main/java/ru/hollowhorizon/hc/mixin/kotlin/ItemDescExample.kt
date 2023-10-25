package ru.hollowhorizon.hc.mixin.kotlin

import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.utils.mcTranslate

fun item(data: String): ItemStack {
    TODO()
}

fun main() {
    item("minecraft:apple").description {
        +"Это яблоко!"
        +"Его можно сьесть!"
        +"my_modpack.translatable_text".mcTranslate
    }
}

private operator fun Component.unaryPlus() {
    TODO("Not yet implemented")
}

private operator fun String.unaryPlus() {
    TODO("Not yet implemented")
}

private fun ItemStack.description(function: () -> Unit) {
    TODO("Not yet implemented")
}
