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
import net.minecraftforge.registries.ForgeRegistries;
import ru.hollowhorizon.hc.client.sounds.HollowSoundHandler;

import javax.annotation.Nonnull;

public class ChoiceButton extends Button {
    int x;
    int y;
    int width;
    int height;
    ITextComponent text;

    public ChoiceButton(int x, int y, int width, int height, ITextComponent text, IPressable onPress) {
        super(x, y, width, height, text, onPress);
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.text = text;
    }

    public ITextComponent getText() {
        return text;
    }

    @Override
    public void playDownSound(SoundHandler soundHandler) {
        soundHandler.play(SimpleSound.forUI(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("hc:choice_button")), 1));
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
        blit(stack, this.x, this.y, 0, isCursorAtButton(x, y) ? this.height : 0, 16, this.height, 16, this.height * 2);
        minecraft.getTextureManager().bind(new ResourceLocation("hc", "textures/gui/lore/button_3.png"));
        blit(stack, this.x + 16, this.y, 0, isCursorAtButton(x, y) ? this.height : 0, this.width - 32, this.height, this.width - 32, this.height * 2);
        minecraft.getTextureManager().bind(new ResourceLocation("hc", "textures/gui/lore/button_2.png"));
        blit(stack, this.x + this.width - 16, this.y, 0, isCursorAtButton(x, y) ? this.height : 0, 16, this.height, 16, this.height * 2);

        stack.translate(0.0D, 0.0D, 120.0D);
        fr.draw(stack, this.text, this.x + this.width / 2F - fr.width(text) / 2F , this.y + this.height / 4f, 0xFFFFFF);

        stack.popPose();
    }

    public boolean isCursorAtButton(int cursorX, int cursorY) {
        return cursorX >= this.x && cursorY >= this.y && cursorX <= this.x + this.width && cursorY <= this.y + this.height;
    }
}
