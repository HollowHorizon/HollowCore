package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.util.Color
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.render.render

inline fun UiScope.Item(stack: ItemStack, scopeName: String? = null, block: ImageScope.() -> Unit = {}) =
    GlCanvas(scopeName, {
        stack.render(x, y, width, height)
    }) {
        modifier.size(16.dp, 16.dp)
        block()
    }
