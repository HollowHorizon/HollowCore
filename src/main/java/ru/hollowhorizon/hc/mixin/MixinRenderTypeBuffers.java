package ru.hollowhorizon.hc.mixin;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeBuffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.utils.tools.GlintRenderType;

@Mixin(RenderTypeBuffers.class)
public class MixinRenderTypeBuffers {

    @Inject(method = "put", at = @At("HEAD"))
    private static void addGlintTypes(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> mapBuildersIn, RenderType renderTypeIn, CallbackInfo callbackInfo) {
        GlintRenderType.addGlintTypes(mapBuildersIn);
    }
}