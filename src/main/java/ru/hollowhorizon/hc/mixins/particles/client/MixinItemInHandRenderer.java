package ru.hollowhorizon.hc.mixins.particles.client;

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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.render.effekseer.internal.EffekFpvRenderer;
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderContext;
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderStateCapture;
import ru.hollowhorizon.hc.client.render.effekseer.render.EffekRenderer;
import ru.hollowhorizon.hc.client.utils.Captures;

import java.util.Objects;

import static ru.hollowhorizon.hc.client.render.effekseer.render.RenderUtil.copyCurrentDepthTo;

@Mixin(value = ItemInHandRenderer.class, priority = 1005)
public class MixinItemInHandRenderer implements EffekFpvRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void resetCaptureState(AbstractClientPlayer player, float f, float g, InteractionHand hand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
        var capture = Captures.INSTANCE.getCAPTURES().computeIfAbsent(hand, arg -> new RenderStateCapture());
        capture.hasCapture = false;
        capture.item = null;
    }

    @Inject(
            method = "renderArmWithItem",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/renderer/block/model/ItemTransforms$TransformType;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    )
    private void setFpvRenderState(AbstractClientPlayer player, float partial, float g, InteractionHand hand, float h, ItemStack stack, float i, PoseStack poseStack, MultiBufferSource buffer, int j, CallbackInfo ci) {
        var stackTop = poseStack.last();
        var capture = Objects.requireNonNull(Captures.INSTANCE.getCAPTURES().get(hand));
        capture.hasCapture = true;
        capture.pose.last().pose().load(stackTop.pose());
        capture.pose.last().normal().load(stackTop.normal());
        capture.projection.load(RenderSystem.getProjectionMatrix());
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
        var oldProjection = RenderSystem.getProjectionMatrix();

        var camera = minecraft.gameRenderer.getMainCamera();
        Captures.INSTANCE.getCAPTURES().forEach((hand, capture) -> {
            if (capture.hasCapture && capture.item != null) {
                RenderSystem.setProjectionMatrix(capture.projection);

                var poseStack = capture.pose;
                poseStack.pushPose();
                poseStack.translate(-0.5, -0.5, -0.5);
                EffekRenderer.onRenderHand(partial, hand, poseStack, capture.projection, camera);
                poseStack.popPose();
            }

            capture.item = null;
        });

        RenderSystem.setProjectionMatrix(oldProjection);

    }
}
