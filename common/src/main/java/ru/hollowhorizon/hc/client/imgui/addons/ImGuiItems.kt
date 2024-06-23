package ru.hollowhorizon.hc.client.imgui.addons

open class ItemProperties {
    open var red = 1f
    open var green = 1f
    open var blue = 1f
    open var alpha = 1f
    open var disableResize = false
    open var rotation = 0f
    open var tooltip = true
    open var scale = 1f
    open var alwaysOnTop = false

    open fun update(hovered: Boolean) {

    }
}