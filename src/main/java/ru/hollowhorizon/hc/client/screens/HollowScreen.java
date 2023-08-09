package ru.hollowhorizon.hc.client.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import ru.hollowhorizon.hc.client.screens.util.Alignment;
import ru.hollowhorizon.hc.client.screens.widget.HollowWidget;
import ru.hollowhorizon.hc.client.screens.widget.layout.ILayoutConsumer;
import ru.hollowhorizon.hc.mixin.ScreenAccessor;

public class HollowScreen extends Screen implements ILayoutConsumer {
    private TextureManager textureManager;

    public HollowScreen(Component screenText) {
        super(screenText);
    }

    public HollowScreen() {
        this(Component.literal(""));
    }

    @Override
    protected void init() {
        this.textureManager = Minecraft.getInstance().textureManager;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        ((ScreenAccessor) this).children().forEach(widget -> widget.mouseMoved(mouseX, mouseY));
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public void tick() {
        ((ScreenAccessor) this).children().forEach(widget -> {
            if (widget instanceof HollowWidget) ((HollowWidget) widget).tick();
        });
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
        this.textureManager.bindForSetup(new ResourceLocation(modid, "textures/" + path));
    }

    public void drawString(PoseStack stack, Component text, Alignment alignment, int offsetX, int offsetY, int color) {
        this.font.draw(
                stack, text,
                getAlignmentPosX(alignment, offsetX, this.width, this.font.width(text)),
                getAlignmentPosY(alignment, offsetY, this.height, this.font.lineHeight),
                color
        );
    }

    public void drawString(PoseStack stack, String text, Alignment positionAlignment, int offsetX, int offsetY, int color) {
        this.font.draw(
                stack, text,
                getAlignmentPosX(positionAlignment, offsetX, this.width, this.font.width(text)),
                getAlignmentPosY(positionAlignment, offsetY, this.height, this.font.lineHeight),
                color
        );
    }

    public void betterBlit(PoseStack stack, Alignment positionAlignment, int offsetX, int offsetY, int targetWidth, int targetHeight) {
        betterBlit(stack, positionAlignment, offsetX, offsetY, targetWidth, targetHeight, targetWidth, targetHeight, 0, 0);
    }

    public void betterBlit(PoseStack stack, Alignment positionAlignment, int offsetX, int offsetY, int targetWidth, int targetHeight, float size) {
        betterBlit(stack, positionAlignment, offsetX, offsetY, targetWidth, targetHeight, targetWidth, targetHeight, 0, 0, size);
    }

    public void betterBlit(PoseStack stack, Alignment alignment, int offsetX, int offsetY, int targetWidth, int targetHeight, int imageWidth, int imageHeight, int texX, int texY) {
        betterBlit(stack, alignment, offsetX, offsetY, targetWidth, targetHeight, imageWidth, imageHeight, texX, texY, 1.0F);
    }

    public void betterBlit(PoseStack stack, Alignment alignment, int offsetX, int offsetY, int targetWidth, int targetHeight, int imageWidth, int imageHeight, int texX, int texY, float size) {
        blit(
                stack,
                getAlignmentPosX(alignment, offsetX, this.width, targetWidth, size),
                getAlignmentPosY(alignment, offsetY, this.height, targetHeight, size),
                texX, texY, (int) (targetWidth * size), (int) (targetHeight * size), (int) (imageWidth * size), (int) (imageHeight * size)
        );
    }

    public void addButtons(AbstractWidget... widgets) {
        for (AbstractWidget w : widgets) {
            this.addRenderableWidget(w);
        }
    }

    @Override
    public boolean mouseReleased(double p_231048_1_, double p_231048_3_, int p_231048_5_) {
        boolean value = false;
        for (GuiEventListener widget : this.children()) {
            value = value || widget.mouseReleased(p_231048_1_, p_231048_3_, p_231048_5_);
        }
        return value;
    }

    @Override
    public boolean mouseDragged(double p_231045_1_, double p_231045_3_, int p_231045_5_, double p_231045_6_, double p_231045_8_) {
        boolean value = false;
        for (GuiEventListener widget : this.children()) {
            value = value || widget.mouseDragged(p_231045_1_, p_231045_3_, p_231045_5_, p_231045_6_, p_231045_8_);
        }
        return value;
    }

    public void betterFillGradient(PoseStack stack, int x, int y, int width, int height, int color1, int color2) {
        fillGradient(stack, x, y, x + width, y + height, 0x66000000, 0xCC000000);
    }

    @Override
    public void addLayoutWidget(@NotNull AbstractWidget widget) {
        this.addRenderableWidget(widget);
    }

    @Override
    public int x() {
        return 0;
    }

    @Override
    public int y() {
        return 0;
    }

    @Override
    public int width() {
        return this.width;
    }

    @Override
    public int height() {
        return this.height;
    }
}
