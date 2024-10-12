package ru.hollowhorizon.hc.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.imgui.ImGuiHandler;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    @Inject(method = "flipFrame", at = @At("HEAD"), remap = false)
    private static void onRenderFrame(long l, CallbackInfo ci) {
        ImGuiHandler.INSTANCE.renderFrames();
    }
}
