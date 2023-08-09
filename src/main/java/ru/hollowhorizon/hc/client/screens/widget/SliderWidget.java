package ru.hollowhorizon.hc.client.screens.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Consumer;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class SliderWidget extends AbstractWidget {
    private static final ResourceLocation SLIDER_BASE = new ResourceLocation(MODID, "textures/gui/buttons/slider_base.png");
    private static final ResourceLocation ARROW_LEFT = new ResourceLocation(MODID, "textures/gui/buttons/arrow_left.png");
    private static final ResourceLocation ARROW_RIGHT = new ResourceLocation(MODID, "textures/gui/buttons/arrow_right.png");
    private float sliderValue = 0;
    private Consumer<Float> valueConsumer;
    private boolean isTyping = false;
    private int mode = 0;
    private float min = 0F;
    private float max = 1F;

    private float multiplier = 1.0F;

    public SliderWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal(""));
        this.x = x;
        this.y = y;
    }

    public void setMax(float max) {
        this.max = max;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setMultiplier(float multiplier) {
        this.multiplier = multiplier;
    }

    public float round(float value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        float result = bd.floatValue();
        if (mode != 2) {
            if (result > max) {
                if (mode == 0) result = max;
                else if (mode == 1) result = 0;
            }
            if (result < min) {
                if (mode == 0) result = min;
                else if (mode == 1) result = 0;
            }
        }
        return result;
    }

    public double getSliderValue() {
        return sliderValue;
    }

    public void setValueConsumer(Consumer<Float> valueConsumer) {
        this.valueConsumer = valueConsumer;
    }

    @Override
    public void playDownSound(SoundManager p_230988_1_) {
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float p_230430_4_) {
        stack.pushPose();
        Minecraft.getInstance().getTextureManager().bindForSetup(SLIDER_BASE);
        blit(stack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        renderLeftArrow(stack, mouseX, mouseY);
        renderRightArrow(stack, mouseX, mouseY);
        stack.popPose();
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int buttonId) {
        if (isCursorAtLeftButton(mouseX, mouseY)) {
            this.sliderValue -= 1F;
        } else if (isCursorAtRightButton(mouseX, mouseY)) {
            this.sliderValue += 1F;
        } else if (isCursorAtMid(mouseX, mouseY)) {
            isTyping = true;
        }
        this.sliderValue = round(this.sliderValue, 3);

        this.valueConsumer.accept(this.sliderValue);
        return super.mouseReleased(mouseX, mouseY, buttonId);
    }

    private boolean isCursorAtMid(double mouseX, double mouseY) {
        return mouseX >= this.x + this.height && mouseX <= this.x + this.width - this.height && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int buttonId, double dragX, double dragY) {
        this.sliderValue += 2F * dragX * multiplier;
        this.sliderValue = round(this.sliderValue, 3);
        this.valueConsumer.accept(this.sliderValue);
        return super.mouseDragged(mouseX, mouseY, buttonId, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double p_231043_1_, double p_231043_3_, double p_231043_5_) {
        this.setValue(this.sliderValue + 0.5F);
        this.valueConsumer.accept(this.sliderValue);
        return false;
    }

    @Override
    public boolean keyPressed(int mouseX, int mouseY, int key) {
        switch (key) {
            case 28:
                isTyping = false;
        }
        return false;
    }

    public void renderLeftArrow(PoseStack stack, int mouseX, int mouseY) {
        stack.pushPose();
        GL11.glPushMatrix();
        if (isCursorAtLeftButton(mouseX, mouseY)) {
            GL11.glColor4f(0.546F, 0.546F, 0.546F, 1.0F);
        }
        Minecraft.getInstance().getTextureManager().bindForSetup(ARROW_LEFT);
        blit(stack, this.x, this.y, 0, 0, this.height, this.height, this.height, this.height);
        GL11.glPopMatrix();
        stack.popPose();
    }

    public void renderRightArrow(PoseStack stack, int mouseX, int mouseY) {
        stack.pushPose();
        GL11.glPushMatrix();
        if (isCursorAtRightButton(mouseX, mouseY)) {
            GL11.glColor4f(0.546F, 0.546F, 0.546F, 1.0F);
        }
        Minecraft.getInstance().getTextureManager().bindForSetup(ARROW_RIGHT);
        blit(stack, this.x + this.width - this.height, this.y, 0, 0, this.height, this.height, this.height, this.height);
        GL11.glPopMatrix();
        stack.popPose();
    }

    public boolean isCursorAtLeftButton(double mouseX, double mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.height && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public boolean isCursorAtRightButton(double mouseX, double mouseY) {
        return mouseX >= this.x + this.width - this.height && mouseX <= this.x + this.width && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public void setValue(double value) {
        this.sliderValue = (float) value;
        this.sliderValue = round(this.sliderValue, 3);
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }
}
