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

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.render.effekseer.internal.EffekFpvRenderer;
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderContext;
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderStateCapture;
import ru.hollowhorizon.hc.client.render.effekseer.render.EffekRenderer;
import ru.hollowhorizon.hc.client.utils.Captures;

import static ru.hollowhorizon.hc.client.render.effekseer.render.RenderUtil.copyCurrentDepthTo;

@Mixin(value = ItemInHandRenderer.class, priority = 1005)
public class MixinItemInHandRenderer implements EffekFpvRenderer {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void resetCaptureState(AbstractClientPlayer player, float f, float g, InteractionHand hand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
        var capture = Captures.INSTANCE.getCAPTURES().computeIfAbsent(hand, arg -> new RenderStateCapture());
        capture.hasCapture = false;
        capture.item = null;
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    )
    private void setFpvRenderState(AbstractClientPlayer player, float partial, float g, InteractionHand hand, float h, ItemStack stack, float i, PoseStack poseStack, MultiBufferSource buffer, int j, CallbackInfo ci) {
        var stackTop = poseStack.last();
        var capture = Captures.INSTANCE.getCAPTURES().get(hand);
        capture.hasCapture = true;
        capture.pose.last().pose().set(stackTop.pose());
        capture.pose.last().normal().set(stackTop.normal());
        capture.projection.set(RenderSystem.getProjectionMatrix());
        capture.item = stack;
    }

    @Inject(method = "renderHandsWithItems", at = @At("RETURN"))
    private void captureHandDepth(float partial, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, LocalPlayer player, int i, CallbackInfo ci) {
        if (RenderContext.renderHandDeferred()) {
            if (RenderContext.captureHandDepth()) {
                copyCurrentDepthTo(RenderStateCapture.CAPTURED_HAND_DEPTH_BUFFER);
            }
        } else {
            hollowcore$renderFpvEffek(partial, player);
        }
    }

    @Override
    public void hollowcore$renderFpvEffek(float partial, @NotNull LocalPlayer player) {
        var oldSorting = RenderSystem.getVertexSorting();
        var oldProjection = RenderSystem.getProjectionMatrix();

        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        Captures.INSTANCE.getCAPTURES().forEach((hand, capture) -> {
            if (capture.hasCapture && capture.item != null) {
                RenderSystem.setProjectionMatrix(capture.projection, oldSorting);

                var poseStack = capture.pose;
                poseStack.pushPose();
                poseStack.translate(-0.5, -0.5, -0.5);
                EffekRenderer.onRenderHand(partial, hand, poseStack, capture.projection, camera);
                poseStack.popPose();
            }

            capture.item = null;
        });

        RenderSystem.setProjectionMatrix(oldProjection, oldSorting);

    }
}
