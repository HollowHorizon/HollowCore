package ru.hollowhorizon.hc.mixin;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.client.textures.GifTexture;

import static ru.hollowhorizon.hc.client.textures.GifTextureKt.GIF_TEXTURES;

@Mixin(TextureManager.class)
public class TextureManagerMixin {

    @Shadow(aliases = "f_118471_") @Final private ResourceManager resourceManager;

    @Inject(method = "getTexture(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/texture/AbstractTexture;", at = @At("HEAD"), cancellable = true)
    public void getTexture(ResourceLocation pPath, CallbackInfoReturnable<AbstractTexture> cir) {
        if (pPath.getPath().endsWith(".gif")) cir.setReturnValue(
                GIF_TEXTURES.computeIfAbsent(pPath, location -> {
                    var texture = new GifTexture(location);
                    texture.load(resourceManager);
                    return texture;
                })
        );
    }
}
