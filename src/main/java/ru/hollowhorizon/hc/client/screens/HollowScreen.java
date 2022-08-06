package ru.hollowhorizon.hc.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import ru.hollowhorizon.hc.client.screens.util.Alignment;

public class HollowScreen extends Screen {
    private TextureManager textureManager;

    protected HollowScreen(ITextComponent screenText) {
        super(screenText);
    }

    @Override
    protected void init() {
        this.textureManager = Minecraft.getInstance().textureManager;
    }

    public static int getAlignmentPosX(Alignment alignment, int offsetX, int positionWidth, int targetWidth) {
        return (int) (positionWidth * alignment.factorX() - targetWidth * alignment.factorX() + offsetX);
    }

    public static int getAlignmentPosX(Alignment alignment, int offsetX, int positionWidth, int targetWidth, float size) {
        return (int) (positionWidth * alignment.factorX() - targetWidth * alignment.factorX() * size + offsetX);
    }

    public static int getAlignmentPosY(Alignment alignment, int offsetY, int positionHeight, int targetHeight) {
        return (int) (positionHeight * alignment.factorY() - targetHeight * alignment.factorY() - offsetY);
    }

    public static int getAlignmentPosY(Alignment alignment, int offsetY, int positionHeight, int targetHeight, float size) {
        return (int) (positionHeight * alignment.factorY() - targetHeight * alignment.factorY() * size - offsetY);
    }

    public void bind(String modid, String path) {
        this.textureManager.bind(new ResourceLocation(modid, "textures/" + path));
    }

    public void drawString(MatrixStack stack, ITextComponent text, Alignment alignment, int offsetX, int offsetY, int color) {
        this.font.draw(
                stack, text,
                getAlignmentPosX(alignment, offsetX, this.width, this.font.width(text)),
                getAlignmentPosY(alignment, offsetY, this.height, this.font.lineHeight),
                color
        );
    }

    public void drawString(MatrixStack stack, String text, Alignment positionAlignment, int offsetX, int offsetY, int color) {
        this.font.draw(
                stack, text,
                getAlignmentPosX(positionAlignment, offsetX, this.width, this.font.width(text)),
                getAlignmentPosY(positionAlignment, offsetY, this.height, this.font.lineHeight),
                color
        );
    }

    public void betterBlit(MatrixStack stack, Alignment positionAlignment, int offsetX, int offsetY, int targetWidth, int targetHeight) {
        betterBlit(stack, positionAlignment, offsetX, offsetY, targetWidth, targetHeight, targetWidth, targetHeight, 0, 0);
    }

    public void betterBlit(MatrixStack stack, Alignment positionAlignment, int offsetX, int offsetY, int targetWidth, int targetHeight, float size) {
        betterBlit(stack, positionAlignment, offsetX, offsetY, targetWidth, targetHeight, targetWidth, targetHeight, 0, 0, size);
    }

    public void betterBlit(MatrixStack stack, Alignment alignment, int offsetX, int offsetY, int targetWidth, int targetHeight, int imageWidth, int imageHeight, int texX, int texY) {
        betterBlit(stack, alignment, offsetX, offsetY, targetWidth, targetHeight, imageWidth, imageHeight, texX, texY, 1.0F);
    }

    public void betterBlit(MatrixStack stack, Alignment alignment, int offsetX, int offsetY, int targetWidth, int targetHeight, int imageWidth, int imageHeight, int texX, int texY, float size) {
        blit(
                stack,
                getAlignmentPosX(alignment, offsetX, this.width, targetWidth, size),
                getAlignmentPosY(alignment, offsetY, this.height, targetHeight, size),
                texX, texY, (int) (targetWidth * size), (int) (targetHeight * size), (int) (imageWidth * size), (int) (imageHeight * size)
        );
    }

    public void addButtons(Widget... widgets) {
        for (Widget w : widgets) {
            this.addButton(w);
        }
    }

    @Override
    public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
        boolean value = false;
        for(Widget widget : this.buttons) {
            value = value || widget.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_);
        }
        return value;
    }

    public void betterFillGradient(MatrixStack stack, int x, int y, int width, int height, int color1, int color2) {
        fillGradient(stack, x, y, x + width, y + height, 0x66000000, 0xCC000000);
    }
}
