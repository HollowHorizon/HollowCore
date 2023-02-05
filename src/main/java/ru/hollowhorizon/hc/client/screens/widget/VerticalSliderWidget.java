package ru.hollowhorizon.hc.client.screens.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;

import java.util.function.Consumer;

public class VerticalSliderWidget extends HollowWidget implements IOriginBlackList {
    private final ResourceLocation texture;
    private int maxHeight;
    private int yHeight;
    private boolean isClicked;
    private Consumer<Float> consumer = (f) -> {};

    public VerticalSliderWidget(int x, int y, int w, int h, ResourceLocation texture) {
        super(x, y, w, h, new StringTextComponent(""));
        this.texture = texture;

        init();

        if (w > h)
            throw new IllegalArgumentException("Width must be less than height, it's a vertical slider! Not a horizontal one!");
    }

    public VerticalSliderWidget(int x, int y, int w, int h) {
        this(x, y, w, h, new ResourceLocation("hc", "textures/gui/buttons/scrollbar.png"));
    }

    @Override
    public void init() {
        super.init();
        this.maxHeight = this.height - 30;
        yHeight = this.y + 15;
    }

    public int clamp(int value) {
        return MathHelper.clamp(value, this.y + 15, this.y + this.height - 15);
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float p_230430_4_) {
        if (isClicked) {
            yHeight = clamp(mouseY);
            this.consumer.accept(getScroll());
        }

        bind(texture);

        //render boarder
        blit(stack, this.x, this.y, this.width * 2, 0, this.width, this.width, this.width * 3, this.width * 3);
        blit(stack, this.x, this.y + this.height - this.width, this.width * 2, this.width * 2, this.width, this.width, this.width * 3, this.width * 3);
        blit(stack, this.x, this.y + this.width, this.width * 2, (this.height - this.width * 2), this.width, this.height - this.width * 2, this.width * 3, (this.height - this.width * 2) * 3);

        //render scroll
        if (mouseY > this.yHeight - 15 && mouseY < this.yHeight + 15 && mouseX > this.x && mouseX < this.x + this.width || isClicked)
            blit(stack, this.x, this.yHeight - 15, this.width, 0, this.width, 30, this.width * 3, 30);
        else blit(stack, this.x, this.yHeight - 15, 0, 0, this.width, 30, this.width * 3, 30);
    }

    public float getScroll() {
        return (this.yHeight - this.y - 15) / (this.maxHeight + 0F);
    }

    public void setScroll(float modifier) {
        this.yHeight = clamp(this.y + (int) (this.maxHeight * modifier) + 15);
    }

    public void onValueChange(Consumer<Float> consumer) {
        this.consumer = consumer;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            isClicked = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            isClicked = false;
            return true;
        }
        return false;
    }
}
