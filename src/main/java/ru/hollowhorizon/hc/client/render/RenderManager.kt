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

package ru.hollowhorizon.hc.client.render

import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import org.joml.Vector3f
import ru.hollowhorizon.hc.api.ParticlesProvider
import ru.hollowhorizon.hc.client.kool.KoolDrawer
import ru.hollowhorizon.hc.client.kool.KoolManager
import ru.hollowhorizon.hc.client.models.internal.manager.GltfManager
import ru.hollowhorizon.hc.client.particles.ParticleVertexConsumerProvider
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderContext.renderLevelDeferred
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderStateCapture
import ru.hollowhorizon.hc.client.render.effekseer.render.EffekRenderer.onRenderWorldLast
import ru.hollowhorizon.hc.client.render.effekseer.render.RenderUtil.copyCurrentDepthTo
import ru.hollowhorizon.hc.client.utils.math.Quaternion
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.client.render.RenderLevelStageEvent
import ru.hollowhorizon.hc.common.events.client.render.RenderStage

object RenderManager {
    fun onInitialize() {
        GltfManager.initialize()
        KoolManager
    }

    @SubscribeEvent
    fun onRender(event: RenderLevelStageEvent) {
        if (event.stage != RenderStage.AFTER_LEVEL) return
        KoolDrawer.draw()
    }

    @SubscribeEvent
    fun onRenderParticles(event: RenderLevelStageEvent) {
        if (event.stage != RenderStage.AFTER_PARTICLES) return

        val poseStack = event.poseStack
        val capture = RenderStateCapture.LEVEL
        val capturedPose = capture.pose.last()
        val camera = event.camera

        capturedPose.pose().set(poseStack.last().pose())
        capturedPose.normal().set(poseStack.last().normal())
        capture.projection.set(event.projectionMatrix)
        capture.camera = camera
        capture.hasCapture = true

        if (renderLevelDeferred()) {
            copyCurrentDepthTo(RenderStateCapture.CAPTURED_WORLD_DEPTH_BUFFER)
        } else {
            onRenderWorldLast(event.partialTick, capture.pose, capture.projection, capture.camera!!)
        }

        val level = Minecraft.getInstance().level as? ParticlesProvider ?: return

        val system = level.system
        if (system.isEmpty()) return

        system.update()

        if (!system.hasAnythingToRender()) return

        val cameraUuid = camera.entity.uuid
        val cameraRotMc = camera.rotation()
        val position = camera.position

        val isFirstPerson = Minecraft.getInstance().options.cameraType == CameraType.FIRST_PERSON
        system.render(
            poseStack,
            Vector3f(position.x.toFloat(), position.y.toFloat(), position.z.toFloat()),
            Quaternion(cameraRotMc.x(), cameraRotMc.y(), cameraRotMc.z(), cameraRotMc.w()),
            ParticleVertexConsumerProvider,
            cameraUuid,
            isFirstPerson
        )
    }
}