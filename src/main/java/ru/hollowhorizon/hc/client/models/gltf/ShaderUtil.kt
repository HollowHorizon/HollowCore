package ru.hollowhorizon.hc.client.models.gltf

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.math.Matrix4f
import net.minecraft.client.renderer.ShaderInstance
import org.lwjgl.opengl.*


inline fun drawWithShader(
    pShaderInstance: ShaderInstance,
    body: () -> Unit
) {
    pShaderInstance.apply()

    pShaderInstance.PROJECTION_MATRIX?.set(RenderSystem.getProjectionMatrix())
    pShaderInstance.PROJECTION_MATRIX?.upload()

    body()

    pShaderInstance.clear()
}