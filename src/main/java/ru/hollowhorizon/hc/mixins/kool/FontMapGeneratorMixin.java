package ru.hollowhorizon.hc.mixins.kool;

import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.utils.ForgeKotlinKt;

import java.awt.*;
import java.io.IOException;
import java.util.Map;

@Mixin(targets = "de/fabmax/kool/platform/FontMapGenerator")
public class FontMapGeneratorMixin {
    @Final
    @Shadow
    private Map<String, Font> customFonts;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(int maxWidth, int maxHeight, CallbackInfo ci) {
        try {
            customFonts.put("monocraft", Font.createFont(Font.TRUETYPE_FONT, ForgeKotlinKt.getStream(new ResourceLocation("hollowcore:fonts/monocraft.ttf"))));
        } catch (FontFormatException | IOException e) {
            HollowCore.LOGGER.error("Failed to load font", e);
        }
    }
}
