package ru.hollowhorizon.hc.client.screens.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;

public class BaseButton extends Button {
    private final ITextComponent text;
    private final ResourceLocation texLocation;

    public BaseButton(int x, int y, int width, int height, ITextComponent text, IPressable onPress, ResourceLocation texture) {
        super(x, y, width, height, text, onPress);
        this.text = text;
        this.texLocation = texture;
    }

    public BaseButton(int x, int y, int width, int height, String text, IPressable onPress, ResourceLocation texture) {
        this(x, y, width, height, new StringTextComponent(text), onPress, texture);
    }

    @Override
    public void render(@Nonnull MatrixStack stack, int x, int y, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fr = minecraft.font;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();

        minecraft.getTextureManager().bind(texLocation);

        blit(stack, this.x, this.y, 0, isCursorAtButton(x, y) ? this.height : 0, this.width, this.height, this.width, this.height * 2);

        stack.pushPose();
        stack.translate(0.0D, 0.0D, 100.0D);
        fr.draw(stack, this.text, this.x + this.width / 2F - fr.width(this.text) / 2F, this.y + this.height / 4f, 0xFFFFFF);
        stack.popPose();
    }

    public boolean isCursorAtButton(int cursorX, int cursorY) {
        return cursorX >= this.x && cursorY >= this.y && cursorX <= this.x + this.width && cursorY <= this.y + this.height;
    }


}
