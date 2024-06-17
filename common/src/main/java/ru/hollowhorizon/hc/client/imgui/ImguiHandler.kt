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

import imgui.*
import imgui.extension.imnodes.ImNodes
import imgui.flag.ImGuiBackendFlags
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiFreeTypeBuilderFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.stream


object ImguiHandler {
    val imGuiGlfw = ImGuiImplGlfw()
    val imGuiGl3 = ImGuiImplGl3()
    var windowHandle: Long = 0

    fun initialize() {
        val window = Minecraft.getInstance().window.window
        initializeImGui(window)
        imGuiGlfw.init(window, true)
        if (!Minecraft.ON_OSX) {
            imGuiGl3.init("#version 410")
        } else {
            imGuiGl3.init("#version 120")
        }

        ImNodes.createContext()
        setupStyle(ImGui.getStyle())
        windowHandle = window
    }

    fun drawFrame(renderable: Renderable) {
        imguiBuffer.clear(Minecraft.ON_OSX)
        Minecraft.getInstance().mainRenderTarget.bindWrite(true)
        imGuiGlfw.newFrame()
        ImGui.newFrame()
        ImGui.setNextWindowViewport(ImGui.getMainViewport().id)

        renderable.getTheme()?.preRender()
        renderable.render()
        renderable.getTheme()?.postRender()

        if (ImGuiMethods.cursorStack.isNotEmpty()) throw StackOverflowError("Cursor stack must be empty!")

        ImGui.render()
        endFrame()

        DockingHelper.DOCKING_ID = 0
    }

    private fun initializeImGui(glHandle: Long) {
        ImGui.createContext()

        val io = ImGui.getIO()
        io.iniFilename = null
        io.addBackendFlags(ImGuiBackendFlags.HasSetMousePos)
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable); // Enable Docking
        io.configViewportsNoTaskBarIcon = true

        val fontAtlas = io.fonts
        val fontConfig = ImFontConfig()
        val rangesBuilder = ImFontGlyphRangesBuilder().apply {
            addRanges(fontAtlas.glyphRangesDefault)
            addRanges(fontAtlas.glyphRangesCyrillic)
            addRanges(fontAtlas.glyphRangesJapanese)
            addRanges(FontAwesomeIcons._IconRange)
        }
        fontConfig.oversampleH = 1
        fontConfig.oversampleV = 1
        fontConfig.fontBuilderFlags = ImGuiFreeTypeBuilderFlags.LoadColor

        val ranges = rangesBuilder.buildRanges()



        fontAtlas.addFontFromMemoryTTF(
            "${HollowCore.MODID}:fonts/monocraft.ttf".rl.stream.readAllBytes(), 30f, fontConfig, ranges
        )
        fontConfig.mergeMode = true
        fontAtlas.addFontFromMemoryTTF(
            "${HollowCore.MODID}:fonts/fa_regular.ttf".rl.stream.readAllBytes(),
            26f,
            fontConfig,
            ranges
        )
        fontAtlas.addFontFromMemoryTTF(
            "${HollowCore.MODID}:fonts/fa_solid.ttf".rl.stream.readAllBytes(),
            26f,
            fontConfig,
            ranges
        )

        fontConfig.pixelSnapH = true
        fontConfig.destroy()

        if (io.hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            val style = ImGui.getStyle()
            style.windowRounding = 0.0f
            style.setColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.WindowBg, 1f))
        }
    }

    fun endFrame() {
        imGuiGl3.renderDrawData(ImGui.getDrawData())
        if (ImGui.getIO().hasConfigFlags(ImGuiConfigFlags.ViewportsEnable)) {
            val backupWindowPtr = GLFW.glfwGetCurrentContext()
            ImGui.updatePlatformWindows()
            ImGui.renderPlatformWindowsDefault()
            GLFW.glfwMakeContextCurrent(backupWindowPtr)
        }
    }

    private fun setupStyle(style: ImGuiStyle) {
        style.windowPadding.set(15f, 15f)
        style.framePadding.set(5.0f, 5.0f)
        style.itemSpacing.set(12.0f, 8.0f)
        style.itemInnerSpacing.set(8f, 6f)
        style.windowRounding = 0f
        style.indentSpacing = 25f
        style.scrollbarSize = 15.0f
        style.scrollbarRounding = 9.0f
        style.grabRounding = 3.0f
        setColor(ImGuiCol.Text, ImVec4(0.80f, 0.80f, 0.83f, 1.00f))
        setColor(ImGuiCol.TextDisabled, ImVec4(0.24f, 0.23f, 0.29f, 1.00f))
        setColor(ImGuiCol.WindowBg, ImVec4(0.06f, 0.05f, 0.07f, 0.50f))
        setColor(ImGuiCol.ChildBg, ImVec4(0.07f, 0.07f, 0.09f, 1.00f))
        setColor(ImGuiCol.PopupBg, ImVec4(0.07f, 0.07f, 0.09f, 1.00f))
        setColor(ImGuiCol.Border, ImVec4(0.80f, 0.80f, 0.83f, 0.88f))
        setColor(ImGuiCol.BorderShadow, ImVec4(0.92f, 0.91f, 0.88f, 0.00f))
        setColor(ImGuiCol.FrameBg, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.FrameBgHovered, ImVec4(0.24f, 0.23f, 0.29f, 1.00f))
        setColor(ImGuiCol.FrameBgActive, ImVec4(0.56f, 0.56f, 0.58f, 1.00f))
        setColor(ImGuiCol.TitleBg, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.TitleBgCollapsed, ImVec4(1.00f, 0.98f, 0.95f, 0.75f))
        setColor(ImGuiCol.TitleBgActive, ImVec4(0.07f, 0.07f, 0.09f, 1.00f))
        setColor(ImGuiCol.MenuBarBg, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.ScrollbarBg, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.ScrollbarGrab, ImVec4(0.80f, 0.80f, 0.83f, 0.31f))
        setColor(ImGuiCol.ScrollbarGrabHovered, ImVec4(0.56f, 0.56f, 0.58f, 1.00f))
        setColor(ImGuiCol.ScrollbarGrabActive, ImVec4(0.06f, 0.05f, 0.07f, 1.00f))
        setColor(ImGuiCol.CheckMark, ImVec4(0.80f, 0.80f, 0.83f, 0.31f))
        setColor(ImGuiCol.SliderGrab, ImVec4(0.80f, 0.80f, 0.83f, 0.31f))
        setColor(ImGuiCol.SliderGrabActive, ImVec4(0.06f, 0.05f, 0.07f, 1.00f))
        setColor(ImGuiCol.Button, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.ButtonHovered, ImVec4(0.24f, 0.23f, 0.29f, 1.00f))
        setColor(ImGuiCol.ButtonActive, ImVec4(0.56f, 0.56f, 0.58f, 1.00f))
        setColor(ImGuiCol.Header, ImVec4(0.10f, 0.09f, 0.12f, 1.00f))
        setColor(ImGuiCol.HeaderHovered, ImVec4(0.56f, 0.56f, 0.58f, 1.00f))
        setColor(ImGuiCol.HeaderActive, ImVec4(0.06f, 0.05f, 0.07f, 1.00f))
        setColor(ImGuiCol.ResizeGrip, ImVec4(0.00f, 0.00f, 0.00f, 0.00f))
        setColor(ImGuiCol.ResizeGripHovered, ImVec4(0.56f, 0.56f, 0.58f, 1.00f))
        setColor(ImGuiCol.ResizeGripActive, ImVec4(0.06f, 0.05f, 0.07f, 1.00f))
        setColor(ImGuiCol.PlotLines, ImVec4(0.40f, 0.39f, 0.38f, 0.63f))
        setColor(ImGuiCol.PlotLinesHovered, ImVec4(0.25f, 1.00f, 0.00f, 1.00f))
        setColor(ImGuiCol.PlotHistogram, ImVec4(0.40f, 0.39f, 0.38f, 0.63f))
        setColor(ImGuiCol.PlotHistogramHovered, ImVec4(0.25f, 1.00f, 0.00f, 1.00f))
        setColor(ImGuiCol.TextSelectedBg, ImVec4(0.25f, 1.00f, 0.00f, 0.43f))
        setColor(ImGuiCol.ModalWindowDimBg, ImVec4(1.00f, 0.98f, 0.95f, 0.73f))
    }

    private fun setColor(colorIndex: Int, color: ImVec4) {
        val style = ImGui.getStyle()
        style.setColor(colorIndex, color.x, color.y, color.z, color.w)
    }
}
