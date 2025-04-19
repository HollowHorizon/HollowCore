package ru.hollowhorizon.hc.common.objects.recipe

import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.Ingredient
import java.util.*

open class ShapelessMatch private constructor(size: Int) {
    val match = IntArray(size)
    val bitSet = BitSet(size * (size + 1))

    private fun augment(l: Int): Boolean {
        if (bitSet[l]) return false
        bitSet.set(l)

        for (r in match.indices) {
            if (bitSet[match.size + l * match.size + r]) {
                if (match[r] == -1 || augment(match[r])) {
                    match[r] = l
                    return true
                }
            }
        }

        return false
    }

    companion object {
        @JvmStatic
        fun matches(stacks: List<ItemStack>, ingredients: List<Ingredient>): Boolean {
            if (stacks.size != ingredients.size) return false

            val m = ShapelessMatch(ingredients.size)

            // Build stack -> ingredient bipartite graph
            for (i in stacks.indices) {
                val stack = stacks[i]

                for (j in ingredients.indices) {
                    if (ingredients[j].test(stack))
                        m.bitSet.set((i + 1) * m.match.size + j)
                }
            }

            // Init matches to -1 (no match)
            Arrays.fill(m.match, -1)

            // Try to find an augmenting path for each stack
            for (i in ingredients.indices) {
                if (!m.augment(i)) return false

                m.bitSet[0, m.match.size] = false
            }

            return true
        }
    }
}
