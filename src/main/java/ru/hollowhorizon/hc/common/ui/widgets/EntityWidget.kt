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