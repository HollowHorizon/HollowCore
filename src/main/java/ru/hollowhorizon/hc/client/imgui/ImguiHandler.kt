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

import imgui.ImFont
import imgui.ImFontConfig
import imgui.ImFontGlyphRangesBuilder
import imgui.ImGui
import imgui.extension.imnodes.ImNodes
import imgui.flag.ImGuiBackendFlags
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiFreeTypeBuilderFlags
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.stream


object ImguiHandler {
    val imGuiGlfw = ImGuiImplGlfw()
    val imGuiGl3 = ImGuiImplGl3()
    var windowHandle: Long = 0
    val FONTS = hashMapOf<Int, ImFont>()

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
        ImGui.styleColorsDark()
        windowHandle = window
    }

    fun drawFrame(renderable: Renderable) {
        imguiBuffer.clear(Minecraft.ON_OSX)
        Minecraft.getInstance().mainRenderTarget.bindWrite(true)
        imGuiGlfw.newFrame()
        ImGui.newFrame()
        ImGui.setNextWindowViewport(ImGui.getMainViewport().id)

        ImGui.pushFont(FONTS[Minecraft.getInstance().window.guiScale.toInt().coerceAtMost(6)])

        renderable.getTheme()?.preRender()
        renderable.render()
        renderable.getTheme()?.postRender()

        ImGui.popFont()

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

        fun loadFont(i: Int, size: Float) {
            FONTS[i] = fontAtlas.addFontFromMemoryTTF(
                "hc:fonts/monocraft.ttf".rl.stream.readAllBytes(), size, fontConfig, ranges
            )
            fontConfig.mergeMode = true
            fontAtlas.addFontFromMemoryTTF("hc:fonts/fa_regular.ttf".rl.stream.readAllBytes(), size, fontConfig, ranges)
            fontAtlas.addFontFromMemoryTTF("hc:fonts/fa_solid.ttf".rl.stream.readAllBytes(), size, fontConfig, ranges)
            fontConfig.mergeMode = false
        }

        loadFont(6, 64f)
        loadFont(5, 48f)
        loadFont(4, 40f)
        loadFont(3, 30f)
        loadFont(2, 20f)
        loadFont(1, 12f)

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
}
