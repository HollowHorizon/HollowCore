package ru.hollowhorizon.hc.client.screens.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import org.lwjgl.opengl.GL11;
import ru.hollowhorizon.hc.common.animations.CameraPoint;

import java.util.ArrayList;
import java.util.List;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class CameraPathWidget extends Widget {
    private static final ResourceLocation BASE = new ResourceLocation(MODID, "textures/gui/buttons/camera_widget.png");
    private static final ResourceLocation OBJECT = new ResourceLocation(MODID, "textures/gui/buttons/camera_widget_object.png");
    private static final ResourceLocation ADD_OBJECT = new ResourceLocation(MODID, "textures/gui/buttons/camera_widget_add_object.png");
    private final List<CameraPoint> points = new ArrayList<>();
    private int startPoint = 0;
    private int currentSlot;

    public CameraPathWidget(int x, int y, int width, int height) {
        super(x, y, width, height, new StringTextComponent(""));
    }

    public void addPoint(CameraPoint point) {
        points.add(point);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (delta < 0) {
            startPoint += 1;
        } else {
            startPoint -= 1;
        }
        if (startPoint < 0) startPoint = 0;
        else if (startPoint > points.size()) startPoint = points.size();
        return false;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float p_230430_4_) {
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        FontRenderer font = Minecraft.getInstance().font;
        int size = this.width / this.height;

        textureManager.bind(BASE);
        blit(stack, this.x, this.y, 0, 0, this.width, this.height, this.width, this.height);
        int i;
        for (i = startPoint; i < startPoint + size; i++) {
            stack.pushPose();
            GL11.glPushMatrix();
            float scale = 1.0F;
            if (i - startPoint == slotHovered(mouseX, mouseY)) {
                scale = 1.1F;
            }
            if (i - startPoint == currentSlot) {
                GL11.glColor4f(0.01F, 0.26F, 0.68F, 1.0F);
            }
            if (i == points.size()) {
                renderAddPoint(textureManager, stack, i - startPoint, scale);
            } else if (i < points.size()) {
                renderPoint(textureManager, font, stack, i - startPoint, startPoint, scale);
            }
            GL11.glPopMatrix();
            stack.popPose();
        }
    }

    private int slotHovered(int mouseX, int mouseY) {
        if (mouseY < this.y || mouseY > this.y + this.height || mouseX < this.x || mouseX > this.width + this.x)
            return -1;
        int size = this.width / this.height;
        for (int i = 1; i < size; i++) {
            int startPos = this.x + this.height * (i - 1);
            int endPos = this.x + this.height * (i);
            if (mouseX >= startPos && mouseX <= endPos) {
                return i - 1;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0)  {
            int slot = slotHovered((int) mouseX, (int) mouseY);
            if (slot != -1) {
                if(startPoint+slot==this.points.size()) {
                    addPoint(new CameraPoint());
                } else if(startPoint+slot < this.points.size()) {
                    currentSlot = slot;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void renderAddPoint(TextureManager textureManager, MatrixStack stack, int id, float scale) {
        textureManager.bind(ADD_OBJECT);

        int oldSize = this.height;
        int size = (int) (oldSize * scale);
        int x = this.x + this.height * id + oldSize / 2 - size / 2;
        int y = this.y + oldSize / 2 - size / 2;

        blit(stack, x, y, 0, 0, size, size, size, size);
    }

    public void renderPoint(TextureManager textureManager, FontRenderer font, MatrixStack stack, int id, int startPos, float scale) {
        textureManager.bind(OBJECT);

        int oldSize = this.height;
        int size = (int) (oldSize * scale);
        int x = this.x + this.height * id + oldSize / 2 - size / 2;
        int y = this.y + oldSize / 2 - size / 2;

        blit(stack, x, y, 0, 0, size, size, size, size);
        font.drawShadow(stack, (id + startPos + 1) + "", this.x + this.height * id + this.height / 2F, this.y + this.height / 2F, 0xFFFFFF);
    }
}
