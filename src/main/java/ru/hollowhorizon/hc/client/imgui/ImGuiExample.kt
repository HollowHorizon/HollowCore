package ru.hollowhorizon.hc.client.imgui

import imgui.ImGui
import imgui.extension.texteditor.TextEditor
import imgui.extension.texteditor.TextEditorLanguageDefinition
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImInt
import imgui.type.ImString
import net.minecraft.client.Minecraft
import java.io.File

val text = ImString("Капец, какой ужас, что за убогий интерфейс... \uD83D\uDE00")
val item = ImInt()

private val EDITOR = TextEditor().apply {
    setLanguageDefinition(TextEditorLanguageDefinition.c().apply {
        setIdentifiers(
            mapOf(
                "npc" to "Возможные функции:\nmoveTo { ... }\nlookAt { ... }\nmoveAlwaysTo { ... }\nlookAlwaysAt { ... }",
                "moveTo" to "Персонаж пойдёт к указанной цели, например: pos(0, 0, 0), team или entity.",
            )
        )
    })
}

fun test() = object : Renderable {
    override fun render() {
        val window = Minecraft.getInstance().window

        ImGui.begin("Test")
        ImGui.text("Hello, world! 📁")
        ImGui.end()
    }

    fun drawTree(file: File) {
        val params =
            if (file.isDirectory) 0 else ImGuiTreeNodeFlags.NoTreePushOnOpen or ImGuiTreeNodeFlags.Leaf or ImGuiTreeNodeFlags.SpanFullWidth

        treeNode(file.name, params) {
            file.listFiles()?.sortedBy { if (it.isDirectory) 0 else 1 }?.forEach {
                drawTree(it)
            }
        }
    }

    override fun getTheme() = object : Theme {
        override fun preRender() {
        }

        override fun postRender() {
        }

    }
}