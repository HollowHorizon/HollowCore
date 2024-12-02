package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.util.MsdfFont
import de.fabmax.kool.util.Time
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.world.Container
import net.minecraft.world.item.Items
import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.client.imgui.GlCanvas
import ru.hollowhorizon.hc.client.kool.KoolManager.MONOCRAFT_DATA
import ru.hollowhorizon.hc.client.render.render
import ru.hollowhorizon.hc.common.containers.ClientContainerManager

fun UiScope.DragStackTooltip() {
    val mouseX = doubleArrayOf(0.0)
    val mouseY = doubleArrayOf(0.0)
    GLFW.glfwGetCursorPos(Minecraft.getInstance().window.window, mouseX, mouseY)

    Popup(mouseX[0].toFloat() - 32f, mouseY[0].toFloat() - 32f) {
        GlCanvas(null, drawItem@{ mouseX, mouseY ->
            val player = Minecraft.getInstance().player ?: return@drawItem

            //ClientContainerManager.PLAYERS_HOLD_STACKS[player.uuid]?.render(leftPx, topPx, contentWidthPx, contentHeightPx)
            Items.BOW.defaultInstance.render(leftPx, topPx, contentWidthPx, contentHeightPx)
        }) {
            modifier.background(null).size(64.dp, 64.dp)
        }
        modifier.background(null)
    }
}

fun UiScope.Slot(container: Container, slot: Int, slotSize: Dimension) = GlCanvas(null, { mouseX, mouseY ->
    var size by remember { mutableStateOf(0f) }

    val hovered = mouseX in leftPx..leftPx + contentWidthPx && mouseY in topPx..topPx + contentHeightPx

    if (hovered) size += Time.deltaT * 5f
    else size -= Time.deltaT * 5f
    size = size.coerceIn(0f, 1f)

    container.getItem(slot).render(leftPx, topPx, contentWidthPx, contentHeightPx, 0.8f + 0.2f * size)
}) {
    Text(container.getItem(slot).count.toString()) {
        modifier.align(AlignmentX.End, AlignmentY.Bottom)
            .font(MsdfFont(MONOCRAFT_DATA, 30f)).zLayer(2000)
    }

    modifier.size(slotSize, slotSize)
        .onClick { pointer ->
            val player = Minecraft.getInstance().player ?: return@onClick
            ClientContainerManager.clickSlot(
                player,
                container,
                container,
                slot,
                pointer.isLeftClick,
                Screen.hasShiftDown()
            )
        }
}