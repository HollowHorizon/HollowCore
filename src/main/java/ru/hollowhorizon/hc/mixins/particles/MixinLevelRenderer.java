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

package ru.hollowhorizon.hc.mixins.particles;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.kool.KoolDrawerKt;
import ru.hollowhorizon.hc.client.particles.ParticleVertexConsumerProvider;
import ru.hollowhorizon.hc.api.ParticlesProvider;
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderContext;
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderStateCapture;
import ru.hollowhorizon.hc.client.render.effekseer.render.EffekRenderer;
import ru.hollowhorizon.hc.client.utils.math.Quaternion;

import javax.annotation.Nullable;
import java.util.UUID;

import static ru.hollowhorizon.hc.client.kool.example.ExampleSceneKt.BEE_DRAWER;
import static ru.hollowhorizon.hc.client.kool.example.ExampleSceneKt.isBeesEnabled;
import static ru.hollowhorizon.hc.client.render.effekseer.render.RenderUtil.copyCurrentDepthTo;


@Mixin(LevelRenderer.class)
public class MixinLevelRenderer {
    @Shadow
    @Nullable
    private ClientLevel level;

    @Inject(method = "renderLevel", at = @At("RETURN"))
    private void onRenderLevelLast(PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci) {
        var capture = RenderStateCapture.LEVEL;
        var capturedPose = capture.pose.last();
        capturedPose.pose().set(poseStack.last().pose());
        capturedPose.normal().set(poseStack.last().normal());
        capture.projection.set(projectionMatrix);
        capture.camera = camera;
        capture.hasCapture = true;

        if (RenderContext.renderLevelDeferred()) {
            copyCurrentDepthTo(RenderStateCapture.CAPTURED_WORLD_DEPTH_BUFFER);
        } else {
            EffekRenderer.onRenderWorldLast(partialTick, capture.pose, capture.projection, capture.camera);
        }

        if(isBeesEnabled) BEE_DRAWER.draw();
        //KoolDrawerKt.getEXAMPLE_SCENE().draw();

        if (!(level instanceof ParticlesProvider)) return;

        var system = ((ParticlesProvider) level).getSystem();
        if (system.isEmpty()) return;

        system.update();

        if (!system.hasAnythingToRender()) return;

        UUID cameraUuid = camera.getEntity().getUUID();
        var cameraRotMc = camera.rotation();
        var position = camera.getPosition();

        boolean isFirstPerson = Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON;
        system.render(poseStack, new Vector3f((float) position.x, (float) position.y, (float) position.z), new Quaternion(cameraRotMc.x(), cameraRotMc.y(), cameraRotMc.z(), cameraRotMc.w()), ParticleVertexConsumerProvider.INSTANCE, cameraUuid, isFirstPerson);

    }
}
