package ru.hollowhorizon.hc.client.imgui.addons

import com.mojang.blaze3d.Blaze3D
import imgui.ImGui
import imgui.flag.*
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.Container
import net.minecraft.world.item.ItemStack
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.imgui.BufferType
import ru.hollowhorizon.hc.client.imgui.ImGuiMethods
import ru.hollowhorizon.hc.client.imgui.currentBufferType
import ru.hollowhorizon.hc.client.utils.math.Interpolation
import ru.hollowhorizon.hc.common.containers.ClientContainerManager
import kotlin.math.abs

class ItemAnimation(var progress: Float = 0f) : ItemProperties() {
    var time = Blaze3D.getTime()
    private var wasHovered = false
    var isPlaced = false

    init {
        disableResize = true
    }

    override fun update(hovered: Boolean) {
        val difference = (Blaze3D.getTime() - time).toFloat().coerceAtMost(0.5f)

        progress = difference * 2

        if (!hovered) progress = 1f - progress

        if (hovered != wasHovered) {
            time = Blaze3D.getTime() - (0.5f - difference)
        }

        scale = 0.9f + 0.2f * Interpolation.EXPO_OUT(progress)

        wasHovered = hovered
    }
}

object ImGuiInventory {
    private var holdStackX = 0f
    private var holdStackY = 0f
    val ITEM_SIZES = HashMap<Container, HashMap<Int, ItemAnimation>>()

    fun ImGuiMethods.slot(
        id: Int,
        stack: ItemStack,
        size: Float = 0f,
        red: Float = 1f,
        green: Float = 1f,
        blue: Float = 1f,
        alpha: Float = 1f,
        container: Container,
    ) {

        val animation = ITEM_SIZES
            .computeIfAbsent(container) { HashMap() }
            .computeIfAbsent(id) { ItemAnimation(0f) }.apply {
                this.red = red
                this.green = green
                this.blue = blue
                this.alpha = alpha
            }

        val pos = ImGui.getCursorScreenPos()
        val isHovering = ImGui.isMouseHoveringRect(pos.x, pos.y, pos.x + size, pos.y + size)
        val light = if (isHovering) 1f else 0f
        val selection = if (animation.isPlaced) 0.35f else 0.15f * light + 0.2f * animation.progress

        ImGui.getWindowDrawList()
            .addRectFilled(
                pos.x, pos.y, pos.x + size, pos.y + size,
                ImGui.colorConvertFloat4ToU32(1f, 1f, 1f, selection)
            )

        item(stack, size, size, id.toString(), false, animation)

        val isLeftClicked = ImGui.isMouseDown(ImGuiMouseButton.Left)
        val isRightClicked = ImGui.isMouseDown(ImGuiMouseButton.Right)


        if (isHovering && (isRightClicked or isLeftClicked) && !animation.isPlaced) {
            val hasShift = Screen.hasShiftDown()
            animation.isPlaced = ClientContainerManager.clickSlot(
                container,
                if (hasShift) ContainerProvider.previousContainer ?: container else container,
                id,
                isLeftClicked,
                hasShift
            )
        }

        if (ImGui.isMouseReleased(ImGuiMouseButton.Left) || ImGui.isMouseReleased(ImGuiMouseButton.Right)) {
            if (animation.isPlaced) animation.time = Blaze3D.getTime()
            animation.isPlaced = false
        }
    }

    internal fun renderHoldItem() {
        val old = currentBufferType
        currentBufferType = BufferType.FOREGROUND

        if (!ClientContainerManager.holdStack.isEmpty) {
            // Если не создавать новое окно, то ImGui сам создаст Debug Window, поэтому создадим невидимое окно.
            ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0f, 0f)
            ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f)
            ImGui.pushStyleColor(ImGuiCol.WindowBg, 0f, 0f, 0f, 0f)
            ImGui.setNextWindowPos(-1000f, -1000f)
            ImGui.setNextWindowSize(1f, 1f)
            ImGui.begin("##hold_stack", ImGuiWindowFlags.NoTitleBar)

            ImGui.setMouseCursor(ImGuiMouseCursor.None)
            ImGui.setCursorScreenPos(
                ImGui.getMousePosX() - 40f,
                ImGui.getMousePosY() - 40f
            )

            val modifier = if (HollowCore.config.inventory.enableItemRotation) 1f else 0f

            ImGuiMethods.item(
                ClientContainerManager.holdStack,
                80f,
                80f,
                properties = ItemProperties().apply {
                    rotation = ((holdStackX + holdStackY) * 2f).coerceIn(-30f, 30f) * modifier
                    tooltip = false
                    disableResize = true
                    alwaysOnTop = true
                },
            )

            updateStackAnimation()
            ImGui.end()
            ImGui.popStyleVar(2)
            ImGui.popStyleColor()
        }

        currentBufferType = old
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