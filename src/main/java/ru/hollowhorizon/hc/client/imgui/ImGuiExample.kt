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

import com.mojang.math.Matrix4f
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import imgui.ImGui
import imgui.extension.imguizmo.ImGuizmo
import imgui.extension.imguizmo.flag.Mode
import imgui.extension.imguizmo.flag.Operation
import imgui.extension.texteditor.TextEditor
import imgui.extension.texteditor.TextEditorLanguageDefinition
import imgui.flag.ImGuiDir
import imgui.type.ImInt
import imgui.type.ImString
import net.minecraft.client.Minecraft
import net.minecraft.util.Mth
import ru.hollowhorizon.hc.client.imgui.ImGuiMethods.centredWindow
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.GltfManager
import ru.hollowhorizon.hc.client.utils.get
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.objects.entities.TestEntity
import ru.hollowhorizon.hc.common.registry.ModEntities.TEST_ENTITY
import kotlin.math.tan

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

val cameraMatrix = floatArrayOf(
    1f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f,
    0f, 0f, 1f, 0f,
    0f, 0f, 0f, 1f,
)
val objectMatrix = floatArrayOf(
    1f, 0f, 0f, 0f,
    0f, 1f, 0f, 0f,
    0f, 0f, 1f, 0f,
    0f, 0f, 0f, 1f,
)

fun FloatArray.identity() {
    for (i in this.indices) {
        if (i == 0 || i == 5 || i == 10 || i == 15) this[i] = 1f
        else this[i] = 0f
    }
}

fun test() = object : Renderable {
    val mob = TestEntity(TEST_ENTITY.get(), Minecraft.getInstance().level!!)
    override fun render() {
        ImGui.setNextWindowSize(1024f, 1024f)
        centredWindow {
            arrowButton("name", ImGuiDir.Down) {

            }
        }
    }
}