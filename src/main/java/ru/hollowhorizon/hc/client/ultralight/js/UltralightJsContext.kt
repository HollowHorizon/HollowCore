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
package ru.hollowhorizon.hc.client.ultralight.js

import com.labymedia.ultralight.UltralightView
import com.labymedia.ultralight.databind.Databind
import com.labymedia.ultralight.databind.DatabindConfiguration
import com.labymedia.ultralight.javascript.JavascriptContext
import com.labymedia.ultralight.javascript.JavascriptObject
import ru.hollowhorizon.hc.client.ultralight.ScreenViewOverlay
import ru.hollowhorizon.hc.client.ultralight.ThreadLock
import ru.hollowhorizon.hc.client.ultralight.UltralightEngine
import ru.hollowhorizon.hc.client.ultralight.ViewOverlay
import ru.hollowhorizon.hc.client.ultralight.js.bindings.*

/**
 * Context setup
 */
class UltralightJsContext(viewOverlay: ViewOverlay, ulView: ThreadLock<UltralightView>) {

    private val contextProvider = ViewContextProvider(ulView)
    val databind = Databind(
        DatabindConfiguration
            .builder()
            .contextProviderFactory(ViewContextProvider.Factory(ulView))
            .build()
    )

    fun setupContext(viewOverlay: ViewOverlay, context: JavascriptContext) {
        val globalContext = context.globalContext
        val globalObject = globalContext.globalObject

        // Pass the view to the context
        setProperty(globalObject, context, "view", viewOverlay)

        setProperty(globalObject, context, "engine", UltralightEngine)
        setProperty(globalObject, context, "client", UltralightJsClient)
        setProperty(globalObject, context, "kotlin", UltralightJsKotlin)

        if (viewOverlay is ScreenViewOverlay) {
            setProperty(globalObject, context, "screen", viewOverlay.adaptedScreen ?: viewOverlay.screen)
            viewOverlay.parentScreen?.let { parentScreen ->
                setProperty(globalObject, context, "parentScreen", parentScreen)
            }
        }
    }

    /**
     * Sets a property on the given object
     */
    private fun setProperty(obj: JavascriptObject, context: JavascriptContext, name: String, value: Any) {
        obj.setProperty(name, databind.conversionUtils.toJavascript(context, value), 0)
    }

}
