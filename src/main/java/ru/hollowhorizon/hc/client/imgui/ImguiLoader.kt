package ru.hollowhorizon.hc.client.imgui

import imgui.ImFontConfig
import imgui.ImFontGlyphRangesBuilder
import imgui.ImGui
import imgui.callback.ImStrConsumer
import imgui.callback.ImStrSupplier
import imgui.extension.imnodes.ImNodes
import imgui.flag.*
import imgui.gl3.ImGuiImplGl3
import imgui.glfw.ImGuiImplGlfw
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFW.*
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.stream
import java.util.*


val renderstack = ArrayList<Renderable>()

fun pushRenderable(renderable: Renderable) {
    renderstack.add(renderable)
}

fun pullRenderable(renderable: Renderable) {
    renderstack.remove(renderable)
}

object ImguiLoader {
    val imGuiGlfw = ImGuiImplGlfw()
    val imGuiGl3 = ImGuiImplGl3()
    var windowHandle: Long = 0

    fun onGlfwInit(handle: Long) {
        initializeImGui(handle)
        imGuiGlfw.init(handle, true)
        if (!Minecraft.ON_OSX) {
            imGuiGl3.init("#version 410")
        } else {
            imGuiGl3.init("#version 120")
        }

        ImNodes.createContext()
        ImGui.styleColorsDark()
        windowHandle = handle
    }

    fun onFrameRender() {
        imGuiGlfw.newFrame()
        ImGui.newFrame()

        for (renderable in renderstack) {
            val profiler = Minecraft.getInstance().profiler
            val name = if (renderable.getName() != null) "/" + renderable.getName() else ""
            profiler.push("ImGui Render$name")
            renderable.getTheme()?.preRender()

            renderable.render()
            renderable.getTheme()?.postRender()
            profiler.pop()
        }

        ImGui.render()
        endFrame()
    }

    private fun initializeImGui(glHandle: Long) {
        ImGui.createContext()

        val io = ImGui.getIO()
        io.iniFilename = null
        io.addBackendFlags(ImGuiBackendFlags.HasSetMousePos)
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard); // Enable Keyboard Controls
        io.configViewportsNoTaskBarIcon = true

        val fontAtlas = io.fonts
        val fontConfig = ImFontConfig()
        val rangesBuilder = ImFontGlyphRangesBuilder().apply {
            addRanges(fontAtlas.glyphRangesCyrillic)
        }
        fontConfig.oversampleH = 1
        fontConfig.oversampleV = 1
        fontConfig.fontBuilderFlags = ImGuiFreeTypeBuilderFlags.LoadColor
        fontAtlas.addFontFromMemoryTTF(
            "hc:fonts/monocraft.ttf".rl.stream.readAllBytes(),
            32f,
            fontConfig,
            rangesBuilder.buildRanges()
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
}
