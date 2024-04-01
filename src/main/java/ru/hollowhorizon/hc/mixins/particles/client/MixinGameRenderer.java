package ru.hollowhorizon.hc.mixins.particles.client;

import com.mojang.blaze3d.vertex.PoseStack;
import ru.hollowhorizon.hc.client.render.effekseer.internal.EffekFpvRenderer;
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderContext;
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderStateCapture;
import ru.hollowhorizon.hc.client.render.effekseer.render.EffekRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static ru.hollowhorizon.hc.client.render.effekseer.render.RenderUtil.pasteToCurrentDepthFrom;
import static org.lwjgl.opengl.GL11.*;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Shadow(aliases = "f_109055_") @Final public ItemInHandRenderer itemInHandRenderer;
    @Shadow(aliases = "f_109059_") @Final
    private Minecraft minecraft;
    @Shadow(aliases = "f_109070_") private boolean renderHand;

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void renderLevelTail(float partial, long l, PoseStack poseStack, CallbackInfo ci) {
        glDepthMask(true);
        glDepthFunc(GL_LEQUAL);

        if (RenderContext.renderLevelDeferred() && RenderStateCapture.LEVEL.hasCapture) {
            RenderStateCapture.LEVEL.hasCapture = false;

            pasteToCurrentDepthFrom(RenderStateCapture.CAPTURED_WORLD_DEPTH_BUFFER);
            assert RenderStateCapture.LEVEL.camera != null;
            EffekRenderer.onRenderWorldLast(partial, RenderStateCapture.LEVEL.pose, RenderStateCapture.LEVEL.projection, RenderStateCapture.LEVEL.camera);
        }
        if (RenderContext.renderHandDeferred() && renderHand) {
            if (RenderContext.captureHandDepth()) {
                pasteToCurrentDepthFrom(RenderStateCapture.CAPTURED_HAND_DEPTH_BUFFER);
            }
            assert minecraft.player != null;
            ((EffekFpvRenderer) itemInHandRenderer).hollowcore$renderFpvEffek(partial, minecraft.player);
        }
    }
}
