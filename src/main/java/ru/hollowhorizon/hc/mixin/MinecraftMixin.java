package ru.hollowhorizon.hc.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.Framebuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow
    public abstract Framebuffer getMainRenderTarget();

//    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/shader/Framebuffer;blitToScreen(II)V"))
//    private void onShaderRender(Framebuffer framebuffer, int screenWidth, int screenHeight) {
//        ModShaders.TEST_SHADER.use();
//        framebuffer.blitToScreen(screenWidth, screenHeight);
//        ModShaders.TEST_SHADER.release();
//    }
}
