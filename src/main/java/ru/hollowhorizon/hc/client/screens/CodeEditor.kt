package ru.hollowhorizon.hc.client.screens

//? if >=1.20.1 {
/*import net.minecraft.client.gui.GuiGraphics
*///?}
import com.mojang.blaze3d.vertex.PoseStack
import imgui.ImGui
import imgui.extension.texteditor.TextEditor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.imgui.ImGuiHandler
import ru.hollowhorizon.hc.client.screens.debug.TextureViewer
import ru.hollowhorizon.hc.common.events.SubscribeEvent
class CodeEditor : Screen(Component.empty()) {
    var popup: Boolean = false
    val editor = TextEditor()
    var index = 0

    @OptIn(DelicateCoroutinesApi::class)
    //? if >=1.20.1 {
    /*override fun render(gui: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    *///?} else {
    override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
        //?}
        renderBackground(
            //? if >=1.21 {
            /*gui, mouseX, mouseY, partialTick
            *///?} elif >=1.20.1 {
            /*gui
            *///?} else {
            poseStack
            //?}
        )

        ImGuiHandler.drawFrame {
            tabBar("Tabs") {
                tabItem("Code") {
                    button("Закрыть") { onClose() }
                    sameLine()
                    button("Запуск") {
                        val text = editor.text
                        val line = editor.cursorPosition.mLine
                        val column = editor.cursorPosition.mColumn
                        var newIndex = 0
                        var lineIndex = 0
                        for (textLine in editor.textLines) {
                            if (lineIndex == line) break
                            newIndex += textLine.length + 1
                            lineIndex++
                        }
                        newIndex += column
                        index = newIndex - 1

                    }

                    editor.render("TextEditor")


                }

                tabItem("Текстуры") {
                    TextureViewer.draw()
                }
            }


        }
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }
}
