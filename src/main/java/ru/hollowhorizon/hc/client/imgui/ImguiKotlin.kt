package ru.hollowhorizon.hc.client.imgui

import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags
import net.minecraft.client.Minecraft

inline fun begin(text: String = "", params: Int = 0, action: () -> Unit) {
    if(ImGui.begin(text, params)) {
        action()
        ImGui.end()
    }
}

fun setWindowSize(width: Float, height: Float) {
    val guiScale = Minecraft.getInstance().window.guiScale.toFloat()
    ImGui.setWindowSize(width * guiScale, height * guiScale)
}

fun setWindowPos(x: Float, y: Float) {
    val guiScale = Minecraft.getInstance().window.guiScale.toFloat()
    ImGui.setWindowPos(x * guiScale, y * guiScale)
}

inline fun treeNode(text: String = "", params: Int = 0, action: () -> Unit) {
    if(ImGui.treeNodeEx(text, params)) {
        action()
        if(params and ImGuiTreeNodeFlags.SpanFullWidth == 0) ImGui.treePop()
    }
}