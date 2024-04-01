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