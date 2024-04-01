package ru.hollowhorizon.hc.client.imgui

fun interface Renderable {
    fun getName(): String? = null

    fun getTheme(): Theme? = null

    fun render()
}

interface Theme {
    fun preRender()

    fun postRender()
}
