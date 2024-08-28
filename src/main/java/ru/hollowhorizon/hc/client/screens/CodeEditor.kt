package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.vertex.PoseStack
import imgui.ImGui
import imgui.extension.texteditor.TextEditor
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
//? if >=1.20.1 {
import net.minecraft.client.gui.GuiGraphics
//?}
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import ru.hollowhorizon.hc.client.imgui.ImGuiHandler
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.scripting.ScriptingCompiler
import ru.hollowhorizon.hc.common.scripting.kotlin.CodeCompletionEvent
import ru.hollowhorizon.hc.common.scripting.kotlin.HollowScript
import ru.hollowhorizon.hc.common.scripting.kotlin.currentCodeIndex
import ru.hollowhorizon.hc.common.scripting.util.CodeCompletion

class CodeEditor : Screen(Component.empty()) {
    var popup: Boolean = false
    val editor = TextEditor()
    var completions = arrayListOf<CodeCompletion>()
    var index = 0

    @OptIn(DelicateCoroutinesApi::class)
    //? if >=1.20.1 {
    override fun render(gui: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
    //?} else {
    /*override fun render(poseStack: PoseStack, mouseX: Int, mouseY: Int, partialTick: Float) {
    *///?}
        renderBackground(
            //? if >=1.21 {
            gui, mouseX, mouseY, partialTick
            //?} elif >=1.20.1 {
            /*gui
            *///?} else {
            /*poseStack
            *///?}
        )

        ImGuiHandler.drawFrame {
            button("Закрыть") { onClose() }
            sameLine()
            button("Запуск") {
                GlobalScope.launch {
                    ScriptingCompiler.compileText<HollowScript>(editor.text).execute()
                }
            }

            editor.render("TextEditor")
            if (editor.isTextChanged) {
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

                if (index >= 0 && index < text.length) {
                    GlobalScope.launch {
                        currentCodeIndex = index
                        ScriptingCompiler.compileText<HollowScript>(text)
                    }
                }
            }

            val completions = ArrayList(completions)

            if (completions.isNotEmpty()) {
                if (ImGui.begin("completions")) {
                    ImGui.beginChild("#internal", minecraft!!.window.height * 0.7f, minecraft!!.window.width / 3f)
                    var close = false
                    completions.forEach {
                        if (it.draw()) {
                            it.complete(editor)
                            close = true
                        }
                        ImGui.separator()
                    }
                    ImGui.endChild()
                    if (close) ImGui.closeCurrentPopup()
                    ImGui.end()
                }
            }

        }
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }
}

@SubscribeEvent
fun onComplete(event: CodeCompletionEvent) {
    (Minecraft.getInstance().screen as? CodeEditor)?.let {
        it.completions.clear()
        it.completions.addAll(event.completions)
    }
}