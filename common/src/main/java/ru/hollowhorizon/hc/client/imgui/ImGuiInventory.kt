package ru.hollowhorizon.hc.client.imgui

import com.mojang.blaze3d.Blaze3D
import imgui.ImGui
import imgui.flag.ImGuiMouseCursor
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.client.imgui.addons.ItemProperties
import ru.hollowhorizon.hc.client.utils.math.Interpolation
import kotlin.math.abs

class ItemAnimation(var progress: Float = 0f) : ItemProperties() {
    var time = Blaze3D.getTime()
    private var wasHovered = false

    init {
        disableResize = true
    }

    override fun update(hovered: Boolean) {
        val difference = (Blaze3D.getTime() - time).toFloat().coerceAtMost(0.5f)

        progress = difference * 2

        if(!hovered) progress = 1f - progress

        if (hovered != wasHovered) {
            time = Blaze3D.getTime() - (0.5f - difference)
        }

        scale = 0.9f + 0.2f * Interpolation.EXPO_OUT(progress)

        wasHovered = hovered
    }
}

object ImGuiInventory {
    private var holdStack = ItemStack.EMPTY
    private var holdStackX = 0f
    private var holdStackY = 0f
    private val ITEM_SIZES = HashMap<Int, ItemAnimation>()

    fun ImGuiMethods.slot(id: Int, stack: ItemStack, size: Float, onChange: (ItemStack) -> Unit) {
        val animation = ITEM_SIZES.computeIfAbsent(id) { ItemAnimation(0f) }
        if (item(
                stack, size, size, id.toString(),
                true, animation
            )
        ) {
            if (!stack.isEmpty && holdStack.isEmpty) {
                holdStack = stack
                onChange(ItemStack.EMPTY)
            } else if (!holdStack.isEmpty && stack !== holdStack) {
                onChange(holdStack)
                holdStack = ItemStack.EMPTY
            }
        }
    }

    internal fun renderHoldItem() {
        if (!holdStack.isEmpty) {
            ImGui.setMouseCursor(ImGuiMouseCursor.None)
            ImGui.setCursorScreenPos(
                ImGui.getMousePosX() - 40f,
                ImGui.getMousePosY() - 40f
            )

            ImGuiMethods.item(
                holdStack,
                80f,
                80f,
                properties = ItemProperties().apply {
                    rotation = ((holdStackX + holdStackY) * 2f).coerceIn(-30f, 30f)
                    tooltip = false
                    disableResize = true
                    alwaysOnTop = true
                },
            )

            updateStackAnimation()
        }
    }

    private fun updateStackAnimation() {
        val delta = ImGui.getIO().mouseDelta
        holdStackX += delta.x / 10f
        holdStackY += delta.y / 10f

        val powerX = Interpolation.EXPO_OUT(abs(holdStackX) / 5)
        val powerY = Interpolation.EXPO_OUT(abs(holdStackY) / 5)

        if (holdStackX > 0) holdStackX = (holdStackX - powerX).coerceAtLeast(0f)
        else if (holdStackX < 0) holdStackX = (holdStackX + powerX).coerceAtMost(0f)

        if (holdStackY > 0) holdStackY = (holdStackY - powerY).coerceAtLeast(0f)
        else if (holdStackY < 0) holdStackY = (holdStackY + powerY).coerceAtMost(0f)

        holdStackX = holdStackX.coerceIn(-25f, 25f)
        holdStackY = holdStackY.coerceIn(-25f, 25f)
    }
}