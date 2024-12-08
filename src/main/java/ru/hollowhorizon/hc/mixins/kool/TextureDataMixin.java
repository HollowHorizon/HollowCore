package ru.hollowhorizon.hc.mixins.kool;

import de.fabmax.kool.pipeline.ImageData;
import de.fabmax.kool.pipeline.TexFormat;
import de.fabmax.kool.util.Buffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.kool.NativeImageBuffer;

@Mixin(value = ImageData.Companion.class, remap = false)
public class TextureDataMixin {
    @Inject(method = "checkBufferFormat", at = @At("HEAD"), cancellable = true)
    private void onGetWindowButtonStyle(Buffer buffer, TexFormat format, CallbackInfo ci) {
        if (buffer instanceof NativeImageBuffer) ci.cancel();
    }
}
