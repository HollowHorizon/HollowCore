package ru.hollowhorizon.hc.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@OnlyIn(Dist.CLIENT)
@Mixin(ResourceLoadProgressGui.class)
public class MixinLoadingScreen {
    @Shadow(remap = false)
    @Final
    private Minecraft minecraft;

    @Inject(method = "registerTextures", at = @At("TAIL"), cancellable = true, remap = false)
    private static void init(Minecraft client, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "render", at = @At(value = "TAIL"), remap = false)
    public void renderBackground(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
//        int width = (int) ((double) this.minecraft.getWindow().getScreenWidth() * 0.5D);
//        int height = (int) ((double) this.minecraft.getWindow().getScreenHeight() * 0.3D);
//        String text = "MasterTech загружается, а я хз, какое сделать меню загрузки";
//        HollowFuckingCore.LOGGER.info(text);
//        minecraft.font.draw(matrixStack, text, width - minecraft.font.width("MasterTech загружается, а я хз, какое сделать меню загрузки") / 2F, height, 0xFFFFFF);
    }
}
