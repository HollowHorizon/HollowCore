package ru.hollowhorizon.hc.client.screens.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import ru.hollowhorizon.hc.client.config.HollowCoreConfig;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class VolumeWidget extends AbstractWidget {
    public static final ResourceLocation VOLUME_SLIDER = new ResourceLocation(MODID, "textures/gui/icons/volume_slider.png");
    private final ResourceLocation VOLUME_ICON = new ResourceLocation(MODID, "textures/gui/icons/volume.png");
    private final ResourceLocation VOLUME_BAR = new ResourceLocation(MODID, "textures/gui/icons/volume_bar.png");
    private final VolumeSlider slider = new VolumeSlider(this.x, this.y, this.width, this.height);
    private boolean lastCursorState = false;
    private boolean renderSlider = false;
    private boolean isMouseDragged = false;

    public VolumeWidget(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal(""));
    }

    @Override
    public void playDownSound(SoundManager p_230988_1_) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int key) {
        if (key == 0) {
            isMouseDragged = true;
        }

        return super.mouseClicked(mouseX, mouseY, key);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int key) {
        if (key == 0) {
            isMouseDragged = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, key);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float p_230430_4_) {
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        boolean isCursorAt = isCursorAtButton(mouseX, mouseY);
        boolean isCursorAtSlider = isCursorAtSlider(mouseX, mouseY);

        tm.bindForSetup(VOLUME_ICON);
        blit(stack, this.x, this.y, 0, isCursorAtSlider ? this.height : 0, this.width / 4, this.height, this.width / 4, this.height * 2);

        if (!lastCursorState && isCursorAt) {
            lastCursorState = true;
            renderSlider = true;
        }

        if (renderSlider) {
            if (!isCursorAtSlider && !isMouseDragged) {
                renderSlider = false;
                return;
            }

            tm.bindForSetup(VOLUME_BAR);
            blit(stack, this.x + this.width / 4, this.y + this.height / 2 - 1, 0, 0, this.width - this.width / 3, this.height / 4, this.width - this.width / 3, this.height / 4);

            if (isMouseDragged) slider.setPos(mouseX);
            slider.render(stack, mouseX, mouseY);

        }
        lastCursorState = isCursorAt;
    }

    public boolean isCursorAtButton(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + this.width / 4 && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    public boolean isCursorAtSlider(int mouseX, int mouseY) {
        return mouseX >= this.x && mouseX <= this.x + (this.width) && mouseY >= this.y && mouseY <= this.y + this.height;
    }

    @Override
    public void updateNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    static class VolumeSlider {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private int position;
        private float posFloat;

        public VolumeSlider(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;

            setPos((int) ((this.width - this.width / 4) * HollowCoreConfig.dialogues_volume * 100) + this.x + this.width / 4);
        }

        public float getPos() {
            return (position - this.x - this.width / 4F) / (this.width - this.width / 4F);
        }

        public void setPos(int pos) {
            if (pos > this.x + this.width / 4 + this.width - this.width / 3) pos = this.x + this.width / 4 + this.width - this.width / 3;
            else if (pos < this.x + this.width / 4) pos = this.x + this.width / 4;
            this.position = pos;
        }

        public void render(PoseStack stack, int x, int y) {
            TextureManager tm = Minecraft.getInstance().getTextureManager();

            tm.bindForSetup(VolumeWidget.VOLUME_SLIDER);
            blit(stack, position - this.width / 8, this.y, 0, isCursorAtSlider(x, y) ? this.height : 0, this.width / 4, this.height, this.width / 4, this.height * 2);
        }

        public boolean isCursorAtSlider(int mouseX, int mouseY) {
            int startPos = position - this.width / 8;

            return mouseX >= startPos && mouseX <= startPos + this.width / 4 && mouseY >= this.y && mouseY <= this.y + this.height;
        }
    }


}
