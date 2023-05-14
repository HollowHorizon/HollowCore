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
package ru.hollowhorizon.hc.client.ultralight

import com.labymedia.ultralight.UltralightView
import com.labymedia.ultralight.config.UltralightViewConfig
import com.labymedia.ultralight.input.UltralightKeyEvent
import com.labymedia.ultralight.input.UltralightMouseEvent
import com.labymedia.ultralight.input.UltralightScrollEvent
import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.client.ultralight.impl.listener.ViewListener
import ru.hollowhorizon.hc.client.ultralight.impl.listener.ViewLoadListener
import ru.hollowhorizon.hc.client.ultralight.impl.renderer.ViewRenderer
import ru.hollowhorizon.hc.client.ultralight.js.UltralightJsContext
import net.minecraft.client.gui.screen.Screen
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.mc

/**
 * A view overlay which is being rendered when the view state is [ViewOverlayState.VISIBLE] or [ViewOverlayState.TRANSITIONING].
 *
 * @param layer The layer to render the view on. This can be either [RenderLayer.OVERLAY_LAYER], [RenderLayer.SPLASH_LAYER] or [RenderLayer.SCREEN_LAYER]
 * @param viewRenderer The renderer to use for this view
 */
open class ViewOverlay(val layer: RenderLayer, private val viewRenderer: ViewRenderer) {

    var state = ViewOverlayState.VISIBLE

    val ultralightView = ThreadLock<UltralightView>()
    val context: UltralightJsContext

    private var garbageCollected = 0L

    private var onStateChange: ((ViewOverlayState) -> Unit)? = null

    init {
        // Setup view
        val (width, height) = mc.window.screenWidth.toLong() to mc.window.screenHeight.toLong()
        val viewConfig = UltralightViewConfig()
            .isTransparent(true)
            .initialDeviceScale(1.0 / Minecraft.getInstance().window.guiScale)

        // Make sure renderer setups config correctly
        viewRenderer.setupConfig(viewConfig)

        ultralightView.lock(UltralightEngine.renderer.get().createView(width, height, viewConfig))
        ultralightView.get().setViewListener(ViewListener())
        ultralightView.get().setLoadListener(ViewLoadListener(this))

        // Setup JS bindings
        context = UltralightJsContext(this, ultralightView)

        HollowCore.LOGGER.debug("Successfully created new view")

        // Fix black screen issue
        resize(width, height)
    }

    /**
     * Loads the specified [url]
     */
    fun loadUrl(url: String) {
        // Unregister listeners
        //context.events._unregisterEvents()

        ultralightView.get().loadURL(url)
        HollowCore.LOGGER.debug("Successfully loaded page $url")
    }

    /**
     * Update view
     */
    fun update() {
        UltralightEngine.renderer.get().update()
        UltralightEngine.renderer.get().render()

        // Collect JS garbage
        collectGarbage()
    }

    /**
     * Render view
     */
    open fun render(matrices: MatrixStack) {
        viewRenderer.render(ultralightView.get(), matrices)
    }

    /**
     * Resizes web view to [width] and [height]
     */
    fun resize(width: Long, height: Long) {
        ultralightView.get().resize(width, height)
        HollowCore.LOGGER.debug("Successfully resized to $width:$height")
    }

    /**
     * Garbage collect JS engine
     */
    private fun collectGarbage() {
        if (garbageCollected == 0L) {
            garbageCollected = System.currentTimeMillis()
        } else if (System.currentTimeMillis() - garbageCollected > 1000) {
            ultralightView.get().lockJavascriptContext().use { lock ->
                lock.context.garbageCollect()
            }
            garbageCollected = System.currentTimeMillis()
        }
    }

    fun state(state: String) {
        this.state = ViewOverlayState.values().firstOrNull { it.jsName.equals(state, true) } ?: return
        onStateChange?.invoke(this.state)
    }

    fun setOnStageChange(stateChange: (ViewOverlayState) -> Unit): ViewOverlay {
        this.onStateChange = stateChange
        return this
    }

    fun free() {
        ultralightView.get().unfocus()
        ultralightView.get().stop()
        viewRenderer.delete()
        //context.events._unregisterEvents()
    }

    fun focus() = ultralightView.get().focus()

    fun unfocus() = ultralightView.get().unfocus()

    fun fireScrollEvent(event: UltralightScrollEvent) = ultralightView.get().fireScrollEvent(event)
    fun fireMouseEvent(event: UltralightMouseEvent) = ultralightView.get().fireMouseEvent(event)
    fun fireKeyEvent(event: UltralightKeyEvent) = ultralightView.get().fireKeyEvent(event)

}

class ScreenViewOverlay(viewRenderer: ViewRenderer, val screen: Screen, val adaptedScreen: Screen?, val parentScreen: Screen?) :
    ViewOverlay(RenderLayer.SCREEN_LAYER, viewRenderer)

enum class ViewOverlayState(val jsName: String) {
    HIDDEN("hidden"),
    VISIBLE("visible"),
    TRANSITIONING("transitioning"),
    END("end")
}
