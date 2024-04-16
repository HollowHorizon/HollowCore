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