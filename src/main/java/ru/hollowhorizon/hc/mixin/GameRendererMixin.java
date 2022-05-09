package ru.hollowhorizon.hc.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/WorldRenderer;doEntityOutline()V"))
    private void onShaderRender(float ticks, long p_195458_2_, boolean p_195458_4_, CallbackInfo ci) {
//        RenderSystem.pushMatrix();
//        ShaderHandler.INSTANCE.applySwirl(ticks);
//        RenderSystem.popMatrix();
    }
}
