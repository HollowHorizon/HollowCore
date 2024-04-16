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

package ru.hollowhorizon.hc.common.ui

import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.api.utils.Polymorphic

interface ScreenPosition {
    var isWidth: Boolean

    operator fun invoke(
        screenWidth: Int,
        screenHeight: Int,
        widgetWidth: Int,
        widgetHeight: Int,
        mouseX: Int,
        mouseY: Int,
    ): Int

    @Serializable
    @Polymorphic(ScreenPosition::class)
    class PercentScreen(private val value: Float, override var isWidth: Boolean) : ScreenPosition {
        override fun invoke(
            screenWidth: Int, screenHeight: Int, widgetWidth: Int, widgetHeight: Int, mouseX: Int, mouseY: Int,
        ) = (if (isWidth) screenWidth * value else screenHeight * value).toInt()
    }

    @Serializable
    @Polymorphic(ScreenPosition::class)
    class PercentWidget(private val value: Float, override var isWidth: Boolean) : ScreenPosition {
        override fun invoke(
            screenWidth: Int, screenHeight: Int, widgetWidth: Int, widgetHeight: Int, mouseX: Int, mouseY: Int,
        ) = (if (isWidth) widgetWidth * value else widgetHeight * value).toInt()
    }

    @Serializable
    @Polymorphic(ScreenPosition::class)
    class Pixels(private val value: Int) : ScreenPosition {
        override var isWidth = false

        override fun invoke(
            screenWidth: Int, screenHeight: Int, widgetWidth: Int, widgetHeight: Int, mouseX: Int, mouseY: Int,
        ) = value
    }

    @Serializable
    @Polymorphic(ScreenPosition::class)
    class Mouse(override var isWidth: Boolean) : ScreenPosition {
        override fun invoke(
            screenWidth: Int, screenHeight: Int, widgetWidth: Int, widgetHeight: Int, mouseX: Int, mouseY: Int,
        ) = if (isWidth) mouseX else mouseY
    }

    @Serializable
    @Polymorphic(ScreenPosition::class)
    class Addition(private val left: ScreenPosition, private val right: ScreenPosition) : ScreenPosition {
        override var isWidth: Boolean
            get() = left.isWidth && left.isWidth
            set(value) {
                left.isWidth = value
                right.isWidth = value
            }

        override fun invoke(
            screenWidth: Int, screenHeight: Int, widgetWidth: Int, widgetHeight: Int, mouseX: Int, mouseY: Int,
        ) = left(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY) +
                right(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)

    }

    @Serializable
    @Polymorphic(ScreenPosition::class)
    class Subtraction(private val left: ScreenPosition, private val right: ScreenPosition) : ScreenPosition {
        override var isWidth: Boolean
            get() = left.isWidth && left.isWidth
            set(value) {
                left.isWidth = value
                right.isWidth = value
            }

        override fun invoke(
            screenWidth: Int, screenHeight: Int, widgetWidth: Int, widgetHeight: Int, mouseX: Int, mouseY: Int,
        ) = left(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY) -
                right(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)

    }

    class Negate(val self: ScreenPosition) : ScreenPosition {
        override var isWidth = self.isWidth
        override fun invoke(
            screenWidth: Int,
            screenHeight: Int,
            widgetWidth: Int,
            widgetHeight: Int,
            mouseX: Int,
            mouseY: Int,
        ): Int {
            return -self(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        }
    }
}