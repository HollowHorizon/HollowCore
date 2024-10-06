package ru.hollowhorizon.hc.client.imgui

import imgui.ImGuiWindowClass
import imgui.flag.ImGuiDir
import imgui.flag.ImGuiWindowFlags
import imgui.internal.ImGui
import imgui.internal.flag.ImGuiDockNodeFlags
import imgui.type.ImInt
import ru.hollowhorizon.hc.client.utils.rl

object GuiEditor {
    fun Graphics.draw() {
        setupDocking("Виджеты", "Интерфейс", "Настройка")

        ImGui.begin("Виджеты")
        ImGui.button("Hello World")
        ImGui.end()

        ImGui.begin("Интерфейс")
        ImGui.text("Тут будет гуишка")
        ImGui.end()

        ImGui.begin("Настройка")
        image("hollowcore:textures/item/joke.png".rl, 150f, 150f)
        ImGui.end()
    }

    fun Graphics.setupDocking(left: String, middle: String, right: String) {
        ImGui.setNextWindowPos(0f, 0f)
        ImGui.setNextWindowSize(screenWidth, screenHeight)

        val shouldDrawWindowContents = ImGui.begin(
            "DockingWindow",
            ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoTitleBar
        )
        val dockspaceID = ImGui.getID("DockingGuiEditor")
        val workspaceWindowClass = ImGuiWindowClass()
        workspaceWindowClass.classId = dockspaceID
        workspaceWindowClass.dockingAllowUnclassed = false
        if (ImGui.dockBuilderGetNode(dockspaceID).ptr == 0L) {
            ImGui.dockBuilderAddNode(
                dockspaceID, ImGuiDockNodeFlags.DockSpace or
                        ImGuiDockNodeFlags.NoWindowMenuButton or ImGuiDockNodeFlags.NoCloseButton
            )

            val region = imgui.ImGui.getContentRegionAvail()
            ImGui.dockBuilderSetNodeSize(dockspaceID, region.x, region.y)

            val leftDockID = ImInt(0)
            val middleDockID = ImInt(0)
            val rightDockID = ImInt(0)

            ImGui.dockBuilderSplitNode(dockspaceID, ImGuiDir.Left, 0.3f, leftDockID, middleDockID)
            ImGui.dockBuilderSplitNode(middleDockID.get(), ImGuiDir.Left, 0.5f, middleDockID, rightDockID)

            val pLeftNode = ImGui.dockBuilderGetNode(leftDockID.get())
            val pMiddleNode = ImGui.dockBuilderGetNode(middleDockID.get())
            val pRightNode = ImGui.dockBuilderGetNode(rightDockID.get())
            val flags = ImGuiDockNodeFlags.NoDockingSplitMe or
                    ImGuiDockNodeFlags.NoDockingOverMe //or ImGuiDockNodeFlags.NoTabBar
            pLeftNode.localFlags = pLeftNode.localFlags or flags
            pMiddleNode.localFlags = pMiddleNode.localFlags or flags
            pRightNode.localFlags = pRightNode.localFlags or flags

            ImGui.dockBuilderDockWindow(left, leftDockID.get())
            ImGui.dockBuilderDockWindow(middle, middleDockID.get())
            ImGui.dockBuilderDockWindow(right, rightDockID.get())

            ImGui.dockBuilderFinish(dockspaceID)
        }
        val dockFlags = if (shouldDrawWindowContents) ImGuiDockNodeFlags.None
        else ImGuiDockNodeFlags.KeepAliveOnly

        val region = ImGui.getContentRegionAvail()

        ImGui.dockSpace(dockspaceID, region.x, region.y, dockFlags, workspaceWindowClass)

        ImGui.end()

        val windowClass = ImGuiWindowClass()
        //windowClass.dockNodeFlagsOverrideSet = ImGuiDockNodeFlags.NoTabBar

        ImGui.setNextWindowClass(windowClass)
    }
}