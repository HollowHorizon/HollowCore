package ru.hollowhorizon.hc.client.screens.debug

import com.mojang.blaze3d.systems.RenderSystem
import imgui.ImGui
import imgui.flag.ImGuiDir
import imgui.type.ImBoolean
import it.unimi.dsi.fastutil.ints.IntArraySet
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import java.util.concurrent.CompletableFuture


object TextureViewer {
    private val openTextures: MutableMap<Int, OpenTexture> = HashMap()
    private val flipX = ImBoolean()
    private val flipY = ImBoolean()
    private lateinit var textures: IntArray
    private var selectedTexture = 0
    private var downloadTextures = false
    private var downloadFuture: CompletableFuture<*>? = null

    private fun scanTextures() {
        val texturesSet = IntArraySet()
        for (i in 0..9999) {
            if (!GL11.glIsTexture(i)) {
                continue
            }

            texturesSet.add(i)
        }

        textures = texturesSet.toIntArray()
        if (textures.size != texturesSet.size) {
            if (!texturesSet.contains(this.selectedTexture)) this.selectedTexture = 0
            this.textures = texturesSet.toIntArray()
            openTextures.keys.removeIf { a ->
                !texturesSet.contains(a)
            }
        }
    }

    fun draw() {
        if (!this::textures.isInitialized) scanTextures()

        val selectedId =
            if (this.selectedTexture < 0 || this.selectedTexture >= this.textures.size) 0
            else this.textures[this.selectedTexture]
        val value = intArrayOf(this.selectedTexture)

        ImGui.beginDisabled(this.textures.isEmpty())
        ImGui.setNextItemWidth(ImGui.getContentRegionAvailX() / 2)
        if (ImGui.sliderInt(
                "##textures",
                value,
                0,
                this.textures.size - 1,
                if (selectedId == 0) "Текстура не существует" else selectedId.toString()
            )
        ) {
            this.selectedTexture = value[0]
        }
        ImGui.endDisabled()
        ImGui.sameLine()

        ImGui.pushButtonRepeat(true)
        ImGui.beginDisabled(this.selectedTexture <= 0)
        if (ImGui.arrowButton("##left", ImGuiDir.Left)) {
            this.selectedTexture--
        }
        ImGui.endDisabled()
        ImGui.beginDisabled(this.selectedTexture >= this.textures.size - 1)
        ImGui.sameLine(0.0f, ImGui.getStyle().itemInnerSpacingX)
        if (ImGui.arrowButton("##right", ImGuiDir.Right)) {
            this.selectedTexture++
        }
        ImGui.endDisabled()
        ImGui.popButtonRepeat()

        ImGui.beginDisabled(this.downloadFuture != null && !this.downloadFuture!!.isDone)
        ImGui.sameLine()
        if (ImGui.button("Сохранить")) {
            this.downloadTextures = true
            this.downloadFuture = CompletableFuture<Any>()
        }
        ImGui.endDisabled()

        ImGui.beginDisabled(this.openTextures.containsKey(selectedId) && this.openTextures[selectedId]?.visible?.get() == true)
        ImGui.sameLine(0.0f, ImGui.getStyle().itemInnerSpacingX)
        if (ImGui.button("Загрузить")) {
            this.openTextures[selectedId] = OpenTexture(this.flipX.get(), this.flipY.get())
        }
        ImGui.endDisabled()

        ImGui.sameLine(0.0f, ImGui.getStyle().itemInnerSpacingX)
        ImGui.checkbox("Отзеркалить по X", this.flipX)

        ImGui.sameLine(0.0f, ImGui.getStyle().itemInnerSpacingX)
        ImGui.checkbox("Отзеркалить по Y", this.flipY)

        if (selectedId != 0) {
            addImage(selectedId, this.flipX.get(), this.flipY.get())
        }
    }

    private fun addImage(selectedId: Int, flipX: Boolean, flipY: Boolean) {
        RenderSystem.bindTexture(selectedId)
        val width: Int = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_WIDTH)
        val height: Int = glGetTexLevelParameteri(GL_TEXTURE_2D, 0, GL_TEXTURE_HEIGHT)
        val size = ImGui.getContentRegionAvailX()
        ImGui.image(
            selectedId,
            size,
            size * height.toFloat() / width.toFloat(),
            (if (flipX) 1 else 0).toFloat(),
            (if (flipY) 1 else 0).toFloat(),
            (if (flipX) 0 else 1).toFloat(),
            (if (flipY) 0 else 1).toFloat(),
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f,
            1.0f
        )
    }


    @JvmRecord
    data class OpenTexture(
        val open: ImBoolean,
        val visible: ImBoolean,
        val flipX: ImBoolean,
        val flipY: ImBoolean,
    ) {
        constructor(flipX: Boolean, flipY: Boolean) : this(
            ImBoolean(),
            ImBoolean(true),
            ImBoolean(flipX),
            ImBoolean(flipY)
        )
    }
}