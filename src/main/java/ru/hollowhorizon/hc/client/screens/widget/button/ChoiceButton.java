package ru.hollowhorizon.hc.client.screens.widget.button;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class ChoiceButton extends Button {

    public ChoiceButton(int x, int y, int width, int height, Component text, OnPress onPress) {
        super(x, y, width, height, text, onPress);
    }

    @Override
    public void playDownSound(SoundManager soundHandler) {
        soundHandler.play(SimpleSoundInstance.forUI(ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("hc:choice_button")), 1));
    }

    @Override
    public void render(@Nonnull PoseStack stack, int x, int y, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        Font fr = minecraft.font;
        stack.pushPose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        minecraft.getTextureManager().bindForSetup(new ResourceLocation("hc", "textures/gui/lore/button_1.png"));
        blit(stack, this.x, this.y, 0, isCursorAtButton(x, y) ? this.height : 0, 16, this.height, 16, this.height * 2);
        minecraft.getTextureManager().bindForSetup(new ResourceLocation("hc", "textures/gui/lore/button_3.png"));
        blit(stack, this.x + 16, this.y, 0, isCursorAtButton(x, y) ? this.height : 0, this.width - 32, this.height, this.width - 32, this.height * 2);
        minecraft.getTextureManager().bindForSetup(new ResourceLocation("hc", "textures/gui/lore/button_2.png"));
        blit(stack, this.x + this.width - 16, this.y, 0, isCursorAtButton(x, y) ? this.height : 0, 16, this.height, 16, this.height * 2);

        stack.translate(0.0D, 0.0D, 120.0D);
        fr.draw(stack, this.getMessage(), this.x + this.width / 2F - fr.width(this.getMessage()) / 2F , this.y + this.height / 4f, 0xFFFFFF);

        stack.popPose();
    }

    public boolean isCursorAtButton(int cursorX, int cursorY) {
        return cursorX >= this.x && cursorY >= this.y && cursorX <= this.x + this.width && cursorY <= this.y + this.height;
    }
}
