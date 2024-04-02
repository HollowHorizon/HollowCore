package ru.hollowhorizon.hc.mixins.particles.client;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderStateCapture;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Inject(method = "resizeDisplay", at = @At("RETURN"))
    private void resizeCapturedDepthBuffer(CallbackInfo ci) {
        final var window = Minecraft.getInstance().getWindow();
        RenderStateCapture.CAPTURED_WORLD_DEPTH_BUFFER.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
        RenderStateCapture.CAPTURED_HAND_DEPTH_BUFFER.resize(window.getWidth(), window.getHeight(), Minecraft.ON_OSX);
    }

}
