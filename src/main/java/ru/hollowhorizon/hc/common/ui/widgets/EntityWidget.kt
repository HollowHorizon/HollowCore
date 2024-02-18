package ru.hollowhorizon.hc.common.ui.widgets

import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import ru.hollowhorizon.hc.api.utils.Polymorphic
import ru.hollowhorizon.hc.client.utils.nbt.ForEntity
import ru.hollowhorizon.hc.common.ui.IWidget
import ru.hollowhorizon.hc.common.ui.ScreenPosition
import ru.hollowhorizon.hc.common.ui.Widget

@Serializable
@Polymorphic(IWidget::class)
class EntityWidget(val entity: @Serializable(ForEntity::class) Entity) : Widget() {
    var scale = 1f
    var entityX: ScreenPosition = 0.px
        set(value) {
            value.isWidth = true
            field = value
        }
    var entityY: ScreenPosition = 0.px
        set(value) {
            value.isWidth = false
            field = value
        }
    var rotationX: ScreenPosition = mouse.apply { isWidth = true }
        set(value) {
            value.isWidth = true
            field = value
        }
    var rotationY: ScreenPosition = mouse.apply { isWidth = false }
        set(value) {
            value.isWidth = false
            field = value
        }

    override fun renderWidget(
        stack: PoseStack,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
        widgetWidth: Int,
        widgetHeight: Int,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        val rotationX = rotationX(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val rotationY = rotationY(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val entityX = entityX(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val entityY = entityY(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)

        InventoryScreen.renderEntityInInventory(
            x + widgetWidth / 2 + entityX,
            y + widgetHeight + entityY,
            (scale * 30).toInt(),
            x + widgetWidth / 2 + entityX - rotationX.toFloat(),
            y + widgetHeight * 0.33f - rotationY + entityY,
            entity as LivingEntity
        )
    }
}

fun Widget.entity(entity: LivingEntity, config: EntityWidget.() -> Unit) {
    this += EntityWidget(entity).apply(config)
}