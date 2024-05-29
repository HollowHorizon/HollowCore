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
import imgui.type.ImInt
import imgui.type.ImString
import net.minecraft.client.Minecraft
import ru.hollowhorizon.hc.client.imgui.ImGuiMethods.centredWindow
import ru.hollowhorizon.hc.common.objects.entities.TestEntity
import ru.hollowhorizon.hc.common.registry.ModEntities.TEST_ENTITY

val text = ImString("Капец, какой ужас, что за убогий интерфейс... \uD83D\uDE00")
val item = ImInt()


fun test() = Renderable {
    ImGui.setNextWindowPos(0f, 0f)
    ImGui.setNextWindowSize(Minecraft.getInstance().window.width.toFloat(), Minecraft.getInstance().window.height.toFloat())
    DockingHelper.splitHorizontally({
        with(ImGuiMethods) {
            node("Скачать") {
                node("Майнкрафт") {
                    node("Бесплатно") {}
                    node("Платно") {}
                }
                node("ГТА") {
                    node("Бесплатно") {}
                    node("Платно") {}
                }
                node("Террарию") {
                    node("Бесплатно") {}
                    node("Платно") {}
                }
            }
        }
    }, {
        with(ImGuiMethods) {
            button("Привет") {}
            sameLine()
            button("Отмена") {}
            tab("Вкладки") {
                tabItem("Ку") {
                    text("Тут текст")
                }
                tabItem("2") {
                    radioButton("hello", true) {}
                }
                tabItem("3") {
                    bulletText("Конец")
                }
            }
        }
    })
}