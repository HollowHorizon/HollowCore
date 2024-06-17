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

package ru.hollowhorizon.hc.client.render.effekseer.render

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.InteractionHand
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import ru.hollowhorizon.hc.client.render.effekseer.EffectDefinition
import ru.hollowhorizon.hc.client.render.effekseer.Effekseer
import ru.hollowhorizon.hc.client.render.effekseer.ParticleEmitter
import ru.hollowhorizon.hc.client.render.effekseer.loader.EffekAssets
import ru.hollowhorizon.hc.client.utils.mc
import java.nio.FloatBuffer
import java.util.concurrent.atomic.AtomicBoolean

object EffekRenderer {
    private val CAMERA_TRANSFORM_BUFFER: FloatBuffer = BufferUtils.createFloatBuffer(16)
    private val PROJECTION_BUFFER: FloatBuffer = BufferUtils.createFloatBuffer(16)
    private val INIT = AtomicBoolean()

    fun init() {
        if (INIT.compareAndExchange(false, true)) {
            return
        }
        if (!Effekseer.init()) throw ExceptionInInitializerError("Failed to initialize Effekseer")
        Runtime.getRuntime().addShutdownHook(Thread(Effekseer::terminate, "ShutdownHook Effekseer::terminate"))
    }

    @JvmStatic
    fun onRenderWorldLast(partialTick: Float, pose: PoseStack, projection: Matrix4f, camera: Camera) {
        draw(ParticleEmitter.Type.WORLD, partialTick, pose, projection, camera)
    }

    @JvmStatic
    fun onRenderHand(
        partialTick: Float,
        hand: InteractionHand,
        pose: PoseStack,
        projection: Matrix4f,
        camera: Camera,
    ) {
        val type = when (hand) {
            InteractionHand.MAIN_HAND -> ParticleEmitter.Type.FIRST_PERSON_MAINHAND
            InteractionHand.OFF_HAND -> ParticleEmitter.Type.FIRST_PERSON_OFFHAND
        }
        draw(type, partialTick, pose, projection, camera)
    }

    private val CAMERA_TRANSFORM_DATA = FloatArray(16)
    private val PROJECTION_MATRIX_DATA = FloatArray(16)

    private fun draw(
        type: ParticleEmitter.Type,
        partialTick: Float,
        pose: PoseStack,
        projection: Matrix4f,
        camera: Camera,
    ) {
        if (!EffekAssets.entries().any { it.value.emitters().anyMatch(ParticleEmitter::isVisible) }) return

        val minecraft = mc
        val w = minecraft.window.width
        val h = minecraft.window.height

        projection.get(PROJECTION_BUFFER)
        transposeMatrix(PROJECTION_BUFFER)
        PROJECTION_BUFFER[PROJECTION_MATRIX_DATA]

        val position = camera.position

        pose.pushPose()

        if (type == ParticleEmitter.Type.WORLD) pose.translate(-position.x(), -position.y(), -position.z())

        pose.last().pose().get(CAMERA_TRANSFORM_BUFFER)
        transposeMatrix(CAMERA_TRANSFORM_BUFFER)
        CAMERA_TRANSFORM_BUFFER[CAMERA_TRANSFORM_DATA]

        pose.popPose()

        minecraft.levelRenderer.particlesTarget?.copyDepthFrom(minecraft.mainRenderTarget)

        val deltaFrames = Minecraft.getInstance().timer.gameTimeDeltaTicks * 3f // 60FPS / 20 TPS

        RenderType.PARTICLES_TARGET.setupRenderState()
        EffekAssets.forEach { _: ResourceLocation, inst: EffectDefinition ->
            inst.draw(type, w, h, CAMERA_TRANSFORM_DATA, PROJECTION_MATRIX_DATA, deltaFrames, partialTick)
        }
        RenderType.PARTICLES_TARGET.clearRenderState()

        CAMERA_TRANSFORM_BUFFER.clear()
        PROJECTION_BUFFER.clear()
    }

    private fun transposeMatrix(m: FloatBuffer) {
        val m00 = m[0]
        val m01 = m[1]
        val m02 = m[2]
        val m03 = m[3]
        val m10 = m[4]
        val m11 = m[5]
        val m12 = m[6]
        val m13 = m[7]
        val m20 = m[8]
        val m21 = m[9]
        val m22 = m[0xA]
        val m23 = m[0xB]
        val m30 = m[0xC]
        val m31 = m[0xD]
        val m32 = m[0xE]
        val m33 = m[0xF]

        m.put(0, m00)
        m.put(1, m10)
        m.put(2, m20)
        m.put(3, m30)
        m.put(4, m01)
        m.put(5, m11)
        m.put(6, m21)
        m.put(7, m31)
        m.put(8, m02)
        m.put(9, m12)
        m.put(0xA, m22)
        m.put(0xB, m32)
        m.put(0xC, m03)
        m.put(0xD, m13)
        m.put(0xE, m23)
        m.put(0xF, m33)
    }

    private val lastDrawTimeByNanos = LongArray(ParticleEmitter.Type.values().size)

    private fun getDeltaTime(type: ParticleEmitter.Type): Float {
        val last = lastDrawTimeByNanos[type.ordinal]
        if (last == 0L) {
            lastDrawTimeByNanos[type.ordinal] = System.nanoTime()
            return 1f / 60
        }

        val now = System.nanoTime()
        lastDrawTimeByNanos[type.ordinal] = now
        return ((now - last) * 1e-9).toFloat()
    }
}
