package ru.hollowhorizon.hc.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import ru.hollowhorizon.hc.client.hollowconfig.HollowCoreConfig;

import static ru.hollowhorizon.hc.common.handlers.HollowEventHandler.ENABLE_BLUR;

public class DialogueOptionsScreen extends Screen {
    private final DialogueScreen lastScreen;

    protected DialogueOptionsScreen(DialogueScreen lastScreen) {
        super(new StringTextComponent("DIALOGUE_OPTIONS_SCREEN"));
        this.lastScreen = lastScreen;
    }

    @Override
    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        renderBackground(p_230430_1_, 0);
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
    }

    @Override
    protected void init() {
        this.buttons.clear();
        this.addButton(new Button(this.width / 2 - 45, this.height - this.height / 3, 90, 20, new TranslationTextComponent("hollowcore.gui.dialogues.save"), (button) -> {
            onClose();
        }));

        IFormattableTextComponent main_hero_voice = new TranslationTextComponent("hollowcore.gui.dialogues.disable_main_hero");
        IFormattableTextComponent enable_blur = new TranslationTextComponent("hollowcore.gui.dialogues.blur");

        if (HollowCoreConfig.main_hero_voice) {
            main_hero_voice.append(new TranslationTextComponent("hollowcore.gui.yes"));
        } else {
            main_hero_voice.append(new TranslationTextComponent("hollowcore.gui.no"));
        }

        if (ENABLE_BLUR) {
            enable_blur.append(new TranslationTextComponent("hollowcore.gui.yes"));
        } else {
            enable_blur.append(new TranslationTextComponent("hollowcore.gui.no"));
        }

        this.addButton(new Button(this.width / 2 - 90, this.height / 2, 180, 20, main_hero_voice, (button) -> {


            this.init();
        }));

        this.addButton(new Button(this.width / 2 - 90, this.height / 2 - 40, 180, 20, enable_blur, (button) -> {
            ENABLE_BLUR = !ENABLE_BLUR;

            this.init();
        }));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(lastScreen);
    }
}
