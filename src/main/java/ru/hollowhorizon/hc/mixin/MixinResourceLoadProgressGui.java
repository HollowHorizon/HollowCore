package ru.hollowhorizon.hc.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ResourceLoadProgressGui;
import net.minecraft.util.ColorHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.textures.VideoTexture;

import static net.minecraft.client.gui.AbstractGui.blit;
import static net.minecraft.client.gui.AbstractGui.fill;
import static ru.hollowhorizon.hc.HollowCore.MODID;

@Mixin(ResourceLoadProgressGui.class)
public class MixinResourceLoadProgressGui {
    private static VideoTexture texture;
    private static int maxCounter;
    @Shadow
    @Final
    private Minecraft minecraft;
    @Shadow
    private long fadeOutStart;
    @Shadow
    private long fadeInStart;
    @Shadow
    @Final
    private boolean fadeIn;
    private int counter = 0;

    @Inject(method = "registerTextures(Lnet/minecraft/client/Minecraft;)V", at = @At("HEAD"), cancellable = true)
    private static void init(Minecraft mc, CallbackInfo ci) {

        texture = new VideoTexture(new ResourceLocation(MODID, "videos/logo_test.mp4"), VideoTexture.VideoOptions.END_AT_LAST_FRAME);
        maxCounter = texture.getLength();

        ci.cancel();
    }

    private static int withAlpha(int alpha) {
        return getBackgroundColor() | alpha << 24;
    }

    private static int getBackgroundColor() {
        return ColorHelper.PackedColor.color(0, 0, 0, 0);
    }

    @Inject(at = @At("TAIL"), method = "render", cancellable = false)
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        int i = this.minecraft.getWindow().getGuiScaledWidth();
        int j = this.minecraft.getWindow().getGuiScaledHeight();
        long l = Util.getMillis();

        float f = this.fadeOutStart > -1L ? (float) (l - this.fadeOutStart) / 1000.0F : -1.0F;
        float g = this.fadeInStart > -1L ? (float) (l - this.fadeInStart) / 500.0F : -1.0F;
        float s;
        int m;

        if (f >= 1.0F) {
            if (this.minecraft.screen != null) {
                this.minecraft.screen.render(matrices, 0, 0, delta);
            }

            m = MathHelper.ceil((1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F)) * 255.0F);
            fill(matrices, 0, 0, i, j, withAlpha(m));
            s = 1.0F - MathHelper.clamp(f - 1.0F, 0.0F, 1.0F);
        } else if (this.fadeIn) {
            if (this.minecraft.screen != null && g < 1.0F) {
                this.minecraft.screen.render(matrices, mouseX, mouseY, delta);
            }

            m = MathHelper.ceil(MathHelper.clamp((double) g, 0.15D, 1.0D) * 255.0D);
            fill(matrices, 0, 0, i, j, withAlpha(m));
            s = MathHelper.clamp(g, 0.0F, 1.0F);
        } else {
            m = getBackgroundColor();
            float p = (float) (m >> 16 & 255) / 255.0F;
            float q = (float) (m >> 8 & 255) / 255.0F;
            float r = (float) (m & 255) / 255.0F;
            GlStateManager._clearColor(p, q, r, 1.0F);
            GlStateManager._clear(16384, Minecraft.ON_OSX);
            s = 1.0F;
        }

        if (counter < maxCounter) {

            texture.bind();

            RenderSystem.enableBlend();
            RenderSystem.blendEquation(32774);
            RenderSystem.blendFunc(770, 1);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, s); //setShaderColor
            blit(matrices, 0, 0, 0, 0, 0, i, j, j, i);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            counter += 1;
        } else {
            counter = 0;
        }
    }
}
