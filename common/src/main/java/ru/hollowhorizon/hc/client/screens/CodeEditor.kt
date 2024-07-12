package ru.hollowhorizon.hc.client.screens

import com.mojang.blaze3d.platform.InputConstants
import imgui.ImGui
import imgui.extension.texteditor.TextEditor
import imgui.flag.ImGuiCol
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import org.jetbrains.kotlin.cli.jvm.compiler.CliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.container.ComponentProvider
import org.jetbrains.kotlin.container.get
import org.jetbrains.kotlin.container.getService
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.BindingTraceContext
import org.jetbrains.kotlin.resolve.LazyTopDownAnalyzer
import org.jetbrains.kotlin.resolve.TopDownAnalysisMode
import org.jetbrains.kotlin.resolve.lazy.declarations.FileBasedDeclarationProviderFactory
import org.jetbrains.kotlin.types.expressions.ExpressionTypingServices
import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.client.imgui.ImGuiHandler
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.scripting.ScriptingCompiler
import ru.hollowhorizon.hc.common.scripting.kotlin.AfterCodeAnalysisEvent
import ru.hollowhorizon.hc.common.scripting.kotlin.HollowScript
import ru.hollowhorizon.hc.common.scripting.kotlin.completions

class CodeEditor : Screen(Component.empty()) {
    var popup: Boolean = false
    val editor = TextEditor()
    var completions = arrayListOf<String>()
    var index = 0

    @OptIn(DelicateCoroutinesApi::class)
    override fun render(guiGraphics: GuiGraphics, mouseX: Int, mouseY: Int, partialTick: Float) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick)
        ImGuiHandler.drawFrame {
            button("Закрыть") { onClose() }
            editor.render("TextEditor")
            if (editor.isTextChanged) {
                val text = editor.text
                val line = editor.cursorPositionLine
                val column = editor.cursorPositionColumn
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
                        ScriptingCompiler.compileText<HollowScript>(text)
                    }
                }
            }

            if (popup) {
                ImGui.openPopup("completions")
                popup = false
            }
            if (completions.isNotEmpty()) {
                if (ImGui.beginPopup("completions")) {
                    val array = (GLFW.GLFW_KEY_SPACE..GLFW.GLFW_KEY_Z).toMutableList()
                    array.add(GLFW.GLFW_KEY_BACKSPACE)

                    for (i in array) {
                        if (InputConstants.isKeyDown(Minecraft.getInstance().window.window, i)) {
                            ImGui.closeCurrentPopup()
                            if (i != GLFW.GLFW_KEY_BACKSPACE) {
                                val char = if (hasShiftDown()) Char(i).uppercase()
                                else Char(i).lowercase()
                                editor.insertText(char)
                            }
                            break
                        }
                    }
                    ImGui.beginChild("#internal", minecraft!!.window.height * 0.7f, minecraft!!.window.width / 3f)
                    var close = false
                    completions.forEach {
                        if (it.contains('(') || it[0].isUpperCase()) {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.61f, 0.25f, 0.96f, 1f)
                        } else {
                            ImGui.pushStyleColor(ImGuiCol.Text, 0.25f, 0.56f, 0.96f, 1f)
                        }
                        if (ImGui.menuItem(it)) {
                            editor.insertText(it.substringBefore(' ').substringBefore('('))
                            close = true
                        }
                        ImGui.popStyleColor()
                    }
                    ImGui.endChild()
                    if (close) ImGui.closeCurrentPopup()
                    ImGui.endPopup()
                }
                if (!ImGui.isPopupOpen("completions")) {
                    completions.clear()
                }
            }
        }
    }

    override fun shouldCloseOnEsc(): Boolean {
        return false
    }
}

@SubscribeEvent
fun onEvent(event: AfterCodeAnalysisEvent) {
    val screen = Minecraft.getInstance().screen as? CodeEditor ?: return

    val environment = event.context.environment

    val (container, trace) = environment.createContainer(event.sourceFiles)
    val module = container.getService(ModuleDescriptor::class.java)

    container.get<LazyTopDownAnalyzer>()
        .analyzeDeclarations(TopDownAnalysisMode.TopLevelDeclarations, event.sourceFiles)

    val services = container.get<ExpressionTypingServices>()

    screen.popup = true
    screen.completions.clear()
    screen.completions += completions(module, trace, event.sourceFiles.first(), screen.index - 1)
}

fun KotlinCoreEnvironment.createContainer(sourcePath: Collection<KtFile>): Pair<ComponentProvider, BindingTraceContext> {
    val trace = CliBindingTrace(project)
    val container = TopDownAnalyzerFacadeForJVM.createContainer(
        project = project,
        files = sourcePath,
        trace = trace,
        configuration = configuration,
        packagePartProvider = ::createPackagePartProvider,
        declarationProviderFactory = ::FileBasedDeclarationProviderFactory
    )
    return Pair(container, trace)
}