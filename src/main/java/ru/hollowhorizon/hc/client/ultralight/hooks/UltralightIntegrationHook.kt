/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2016 - 2023 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package ru.hollowhorizon.hc.client.ultralight.hooks

import ru.hollowhorizon.hc.client.ultralight.UltralightEngine

/**
 * A integration bridge between Minecraft and Ultralight
 */
object UltralightIntegrationHook {

    val gameRenderHandlerHandler = {
        UltralightEngine.update()
    }

    val screenRenderHandler = {
        //UltralightEngine.render(RenderLayer.SCREEN_LAYER, it.matrices)
        //UltralightEngine.render(RenderLayer.SPLASH_LAYER, it.matrices)
    }

    val overlayRenderHandler = {
        //UltralightEngine.render(RenderLayer.OVERLAY_LAYER, it.matrices)
    }

    val windowResizeWHandler = {
        //UltralightEngine.resize(it.width.toLong(), it.height.toLong())
    }

    val windowFocusHandler = {
        //UltralightEngine.inputAdapter.focusCallback(it.window, it.focused)
    }

    val mouseButtonHandler = {
        //UltralightEngine.inputAdapter.mouseButtonCallback(it.window, it.button, it.action, it.mods)
    }

    val mouseScrollHandler = {
        //UltralightEngine.inputAdapter.scrollCallback(it.window, it.horizontal, it.vertical)
    }

    val mouseCursorHandler = {
        //UltralightEngine.inputAdapter.cursorPosCallback(it.window, it.x, it.y)
    }

    val keyboardKeyHandler = {
        //UltralightEngine.inputAdapter.keyCallback(it.window, it.keyCode, it.scancode, it.action, it.mods)
    }

    val keyboardCharHandler = {
        //UltralightEngine.inputAdapter.charCallback(it.window, it.codepoint)
    }

}
