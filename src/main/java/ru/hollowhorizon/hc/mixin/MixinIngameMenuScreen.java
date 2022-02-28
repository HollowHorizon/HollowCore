package ru.hollowhorizon.hc.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.IngameMenuScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.hollowhorizon.hc.client.screens.CameraScreen;
import ru.hollowhorizon.hc.client.screens.widget.button.MenuButton;
import ru.hollowhorizon.hc.common.network.NetworkHandler;
import ru.hollowhorizon.hc.common.network.messages.OpenEventListToServer;

@Mixin(IngameMenuScreen.class)
public class MixinIngameMenuScreen extends Screen {
    private MixinIngameMenuScreen() {
        super(new TranslationTextComponent("mainMenuFix"));

    }

    @Inject(method = "createPauseMenu", at = @At("HEAD"))
    private void init(CallbackInfo ci) {
        this.addButton(
                new MenuButton(0, 0, 20, 20,
                        new StringTextComponent(""), (button) -> Minecraft.getInstance().setScreen(new CameraScreen())));
    }
}
