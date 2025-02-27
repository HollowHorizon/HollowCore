package ru.hollowhorizon.hc.mixins.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.common.events.EventBus;
import ru.hollowhorizon.hc.common.events.client.ScreenEvent;

@Mixin(Screen.class)
public class ScreenMixin {
    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("HEAD"))
    private void beforeInit(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        var screen = (Screen) (Object) this;
        var event = new ScreenEvent.Open(screen);
        EventBus.post(event);
        if(screen != event.getScreen()) {
            Minecraft.getInstance().setScreen(event.getScreen());
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemove(CallbackInfo ci) {
        var screen = (Screen) (Object) this;
        var event = new ScreenEvent.Close(screen);
        EventBus.post(event);
    }
}
