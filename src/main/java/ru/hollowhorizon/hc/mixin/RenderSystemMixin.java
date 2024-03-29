package ru.hollowhorizon.hc.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.imgui.ImguiLoader;

@Mixin(RenderSystem.class)
public class RenderSystemMixin {
    private RenderSystemMixin() {
    }

    @Inject(method = "flipFrame", at = @At("HEAD"))
    private static void runTickTail(CallbackInfo ci) {
        //Minecraft.getInstance().getProfiler().push("ImGui Render");
        //ImguiLoader.INSTANCE.onFrameRender();
        //Minecraft.getInstance().getProfiler().pop();
    }
}
