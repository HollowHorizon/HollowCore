package ru.hollowhorizon.hc.client.screens.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import ru.hollowhorizon.hc.client.sounds.HollowSoundHandler;

import javax.annotation.Nonnull;

public class ChoiceButton extends Button {
    int x;
    int y;
    int width;
    int height;
    public int choice;
    ITextComponent text;

    public ChoiceButton(int x, int y, int width, int height, ITextComponent text, IPressable onPress, int choice) {
        super(x, y, width, height, text, onPress);
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.text = text;
        this.choice = choice;
    }

    public ITextComponent getText() {
        return text;
    }

    @Override
    public void playDownSound(SoundHandler soundHandler) {
        soundHandler.play(SimpleSound.forUI(HollowSoundHandler.CHOICE_BUTTON, 1));
    }

    @Override
    public void render(@Nonnull MatrixStack stack, int x, int y, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fr = minecraft.font;
        stack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();

        minecraft.getTextureManager().bind(new ResourceLocation("hc", "textures/gui/lore/button_1.png"));
        blit(stack, this.x - 16, this.y, 0, 0, 16, this.height, 16, this.height);
        minecraft.getTextureManager().bind(new ResourceLocation("hc", "textures/gui/lore/button_3.png"));
        blit(stack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        minecraft.getTextureManager().bind(new ResourceLocation("hc", "textures/gui/lore/button_2.png"));
        blit(stack, this.x + this.width, this.y, 0, 0, 16, this.height, 16, this.height);

        stack.translate(0.0D, 0.0D, 120.0D);
        fr.draw(stack, this.text, this.x, this.y + this.height / 4f, 0xFFFFFF);
        stack.popPose();
    }
}
