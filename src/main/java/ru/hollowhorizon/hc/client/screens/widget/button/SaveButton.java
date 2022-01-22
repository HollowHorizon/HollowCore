package ru.hollowhorizon.hc.client.screens.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class SaveButton extends Button {
    private final ResourceLocation start = new ResourceLocation(MODID, "textures/gui/buttons/save_button_start.png");
    private final ResourceLocation end = new ResourceLocation(MODID, "textures/gui/buttons/save_button_end.png");
    private final ResourceLocation top = new ResourceLocation(MODID, "textures/gui/buttons/save_button_top.png");
    private final ResourceLocation startHovered = new ResourceLocation(MODID, "textures/gui/buttons/save_button_start_hover.png");
    private final ResourceLocation endHovered = new ResourceLocation(MODID, "textures/gui/buttons/save_button_end_hover.png");
    private final ResourceLocation topHovered = new ResourceLocation(MODID, "textures/gui/buttons/save_button_top_hover.png");

    public SaveButton(int x, int y, int width, int height, ITextComponent text, IPressable onPress) {
        super(x, y, Minecraft.getInstance().font.width(text) + 40, height, text, onPress);
    }

    @Override
    public void onPress() {
        super.onPress();
    }

    @Override
    public void render(MatrixStack stack, int x, int y, float f) {
        Minecraft minecraft = Minecraft.getInstance();
        FontRenderer fr = minecraft.font;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        if (isCursorAtButton(x, y)) {
            minecraft.getTextureManager().bind(startHovered);
            blit(stack, this.x - 20, this.y, 0.0F, 0.0F, 20, this.height, 20, this.height);
            minecraft.getTextureManager().bind(topHovered);
            blit(stack, this.x, this.y, 0.0F, 0.0F, fr.width(this.getMessage()), this.height, fr.width(this.getMessage()), this.height);
            minecraft.getTextureManager().bind(endHovered);
        } else {
            minecraft.getTextureManager().bind(start);
            blit(stack, this.x - 20, this.y, 0.0F, 0.0F, 20, this.height, 20, this.height);
            minecraft.getTextureManager().bind(top);
            blit(stack, this.x, this.y, 0.0F, 0.0F, fr.width(this.getMessage()), this.height, fr.width(this.getMessage()), this.height);
            minecraft.getTextureManager().bind(end);
        }
        blit(stack, this.x + fr.width(this.getMessage()), this.y, 0.0F, 0.0F, 20, this.height, 20, this.height);

        stack.pushPose();
        stack.translate(0.0D, 0.0D, 100.0D);
        fr.draw(stack, this.getMessage(), (float) this.x, (float) this.y + (float) this.height / 4.0F, 0x000000);
        stack.popPose();
    }

    public boolean isCursorAtButton(int cursorX, int cursorY) {
        return cursorX >= this.x && cursorY >= this.y && cursorX <= this.x + this.width && cursorY <= this.y + this.height;
    }
}
