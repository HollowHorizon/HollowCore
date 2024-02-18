package ru.hollowhorizon.hc.mixin;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.hollowhorizon.hc.api.IAutoScaled;
import ru.hollowhorizon.hc.client.utils.JavaHacks;

@Mixin(Window.class)
public class WindowMixin {
    @Inject(method = "getGuiScale", at = @At("HEAD"), cancellable = true)
    public void getGuiScale(CallbackInfoReturnable<Double> cir) {
        Window window = JavaHacks.forceCast(this);

        if (!(Minecraft.getInstance().screen instanceof IAutoScaled)) return;

        cir.setReturnValue((double) window.calculateScale(0, Minecraft.getInstance().isEnforceUnicode()));
    }

    @Inject(method = "getGuiScaledHeight", at = @At("HEAD"), cancellable = true)
    public void getGuiScaledHeight(CallbackInfoReturnable<Integer> cir) {
        Window window = JavaHacks.forceCast(this);

        if (!(Minecraft.getInstance().screen instanceof IAutoScaled)) return;

        double scale = window.calculateScale(0, Minecraft.getInstance().isEnforceUnicode());
        int height = (int) (window.getHeight() / scale);

        cir.setReturnValue((window.getHeight() / scale > height ? height + 1 : height));
    }

    @Inject(method = "getGuiScaledWidth", at = @At("HEAD"), cancellable = true)
    public void getGuiScaledWidth(CallbackInfoReturnable<Integer> cir) {
        Window window = JavaHacks.forceCast(this);

        if (!(Minecraft.getInstance().screen instanceof IAutoScaled)) return;

        double scale = window.calculateScale(0, Minecraft.getInstance().isEnforceUnicode());
        int width = (int) (window.getWidth() / scale);

        cir.setReturnValue((window.getWidth() / scale > width ? width + 1 : width));
    }
}
