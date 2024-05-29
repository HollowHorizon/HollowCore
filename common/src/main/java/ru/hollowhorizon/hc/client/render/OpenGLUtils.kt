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

package ru.hollowhorizon.hc.client.render

import com.mojang.blaze3d.vertex.BufferBuilder
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.inventory.InventoryScreen
import net.minecraft.world.entity.LivingEntity
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3d
import org.joml.Vector3f

object OpenGLUtils {
    fun drawLine(
        bufferbuilder: BufferBuilder, matrix: Matrix4f,
        from: Vector3d, to: Vector3d,
        r: Float, g: Float, b: Float, a: Float,
    ) {
        bufferbuilder
            .vertex(matrix, from.x.toFloat(), from.y.toFloat() - 0.1f, from.z.toFloat())
            .color(r, g, b, a)
            .endVertex()
        bufferbuilder
            .vertex(matrix, to.x.toFloat(), to.y.toFloat() - 0.1f, to.z.toFloat())
            .color(r, g, b, a)
            .endVertex()
    }
}


fun LivingEntity.render(
    guiGraphics: GuiGraphics,
    x: Float,
    y: Float,
    scale: Float,
    mouseX: Float,
    mouseY: Float,
) {
    InventoryScreen.renderEntityInInventory(
        guiGraphics,
        x,
        y,
        scale,
        Vector3f(),
        Quaternionf(),
        Quaternionf(),
        this
    )
}