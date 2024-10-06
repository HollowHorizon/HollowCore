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
import imgui.ImGuiWindowClass
import imgui.flag.ImGuiDir
import imgui.flag.ImGuiDockNodeFlags
import imgui.flag.ImGuiWindowFlags
import imgui.type.ImInt
import net.minecraft.client.Minecraft

object DockingHelper {
    var DOCKING_ID = 0

    fun splitHorizontally(left: () -> Unit, right: () -> Unit, ratio: Float = 0.5f) {
        ImGui.setNextWindowPos(0f, 0f)
        val window = Minecraft.getInstance().window
        ImGui.setNextWindowSize(window.width.toFloat(), window.height.toFloat())
        val shouldDrawWindowContents = ImGui.begin(
            "DockingWindow",
            ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoTitleBar
        )
        val dockspaceID = ImGui.getID("MyWindow_DockSpace_${DOCKING_ID++}")
        val workspaceWindowClass = ImGuiWindowClass()
        workspaceWindowClass.classId = dockspaceID
        workspaceWindowClass.dockingAllowUnclassed = false

        if (imgui.internal.ImGui.dockBuilderGetNode(dockspaceID).ptr == 0L) {
            imgui.internal.ImGui.dockBuilderAddNode(
                dockspaceID, imgui.internal.flag.ImGuiDockNodeFlags.DockSpace or
                        imgui.internal.flag.ImGuiDockNodeFlags.NoWindowMenuButton or
                        imgui.internal.flag.ImGuiDockNodeFlags.NoCloseButton
            )
            val region = ImGui.getContentRegionAvail()
            imgui.internal.ImGui.dockBuilderSetNodeSize(dockspaceID, region.x, region.y)

            val leftDockID = ImInt(0)
            val rightDockID = ImInt(0)
            imgui.internal.ImGui.dockBuilderSplitNode(dockspaceID, ImGuiDir.Left, ratio, leftDockID, rightDockID);

            val pLeftNode = imgui.internal.ImGui.dockBuilderGetNode(leftDockID.get())
            val pRightNode = imgui.internal.ImGui.dockBuilderGetNode(rightDockID.get())
            pLeftNode.localFlags = pLeftNode.localFlags or imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoDockingSplitMe or imgui.internal.flag.ImGuiDockNodeFlags.NoDockingOverMe or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar
            pRightNode.localFlags = pRightNode.localFlags or imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoDockingSplitMe or imgui.internal.flag.ImGuiDockNodeFlags.NoDockingOverMe or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar

            // Dock windows
            imgui.internal.ImGui.dockBuilderDockWindow("##LeftPanel", leftDockID.get())
            imgui.internal.ImGui.dockBuilderDockWindow("##RightPanel", rightDockID.get())

            imgui.internal.ImGui.dockBuilderFinish(dockspaceID)
        }

        val dockFlags = if (shouldDrawWindowContents) ImGuiDockNodeFlags.None
        else ImGuiDockNodeFlags.KeepAliveOnly
        val region = ImGui.getContentRegionAvail()
        ImGui.dockSpace(dockspaceID, region.x, region.y, dockFlags, workspaceWindowClass)
        ImGui.end()

        val windowClass = ImGuiWindowClass()
        windowClass.dockNodeFlagsOverrideSet = imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar

        ImGui.setNextWindowClass(windowClass)

        ImGui.begin("##LeftPanel")
        left()
        ImGui.end()

        ImGui.begin("##RightPanel")
        right()
        ImGui.end()
    }

    fun splitVertically(left: () -> Unit, right: () -> Unit, ratio: Float = 0.5f) {
        val window = Minecraft.getInstance().window
        val size = ImGui.getContentRegionMax()
        ImGui.setNextWindowSize(size.x, size.y)
        val shouldDrawWindowContents = ImGui.begin(
            "DockingWindow",
            ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoCollapse or ImGuiWindowFlags.NoTitleBar
        )
        val dockspaceID = ImGui.getID("MyWindow_DockSpace_${DOCKING_ID++}")
        val workspaceWindowClass = ImGuiWindowClass()
        workspaceWindowClass.setClassId(dockspaceID)
        workspaceWindowClass.dockingAllowUnclassed = false

        if (imgui.internal.ImGui.dockBuilderGetNode(dockspaceID).ptr == 0L) {
            imgui.internal.ImGui.dockBuilderAddNode(
                dockspaceID, imgui.internal.flag.ImGuiDockNodeFlags.DockSpace or
                        imgui.internal.flag.ImGuiDockNodeFlags.NoWindowMenuButton or
                        imgui.internal.flag.ImGuiDockNodeFlags.NoCloseButton
            )
            val region = ImGui.getContentRegionAvail()
            imgui.internal.ImGui.dockBuilderSetNodeSize(dockspaceID, size.x, size.y)

            val leftDockID = ImInt(0)
            val rightDockID = ImInt(0)
            imgui.internal.ImGui.dockBuilderSplitNode(dockspaceID, ImGuiDir.Up, ratio, leftDockID, rightDockID);

            val pLeftNode = imgui.internal.ImGui.dockBuilderGetNode(leftDockID.get())
            val pRightNode = imgui.internal.ImGui.dockBuilderGetNode(rightDockID.get())
            pLeftNode.localFlags = pLeftNode.localFlags or imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoDockingSplitMe or imgui.internal.flag.ImGuiDockNodeFlags.NoDockingOverMe or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar
            pRightNode.localFlags = pRightNode.localFlags or imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoDockingSplitMe or imgui.internal.flag.ImGuiDockNodeFlags.NoDockingOverMe or
                    imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar

            // Dock windows
            imgui.internal.ImGui.dockBuilderDockWindow("##UpPanel", leftDockID.get())
            imgui.internal.ImGui.dockBuilderDockWindow("##DownPanel", rightDockID.get())

            imgui.internal.ImGui.dockBuilderFinish(dockspaceID)
        }

        val dockFlags = if (shouldDrawWindowContents) ImGuiDockNodeFlags.None
        else ImGuiDockNodeFlags.KeepAliveOnly
        val region = ImGui.getContentRegionAvail()
        ImGui.dockSpace(dockspaceID, region.x, region.y, dockFlags, workspaceWindowClass)
        ImGui.end()

        val windowClass = ImGuiWindowClass()
        windowClass.dockNodeFlagsOverrideSet = imgui.internal.flag.ImGuiDockNodeFlags.NoTabBar

        ImGui.setNextWindowClass(windowClass)

        ImGui.begin("##UpPanel")
        left()
        ImGui.end()

        ImGui.begin("##DownPanel")
        right()
        ImGui.end()
    }
}