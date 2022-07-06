package ru.hollowhorizon.hc.client.screens.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import ru.hollowhorizon.hc.client.screens.util.Alignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.hollowhorizon.hc.client.screens.HollowScreen.getAlignmentPosX;
import static ru.hollowhorizon.hc.client.screens.HollowScreen.getAlignmentPosY;

public class HollowWidget extends Widget {
    protected final List<Widget> widgets = new ArrayList<>();
    protected final TextureManager textureManager;
    protected final FontRenderer font;


    public HollowWidget(int x, int y, int width, int height, ITextComponent text) {
        super(x, y, width, height, text);
        this.textureManager = Minecraft.getInstance().textureManager;
        this.font = Minecraft.getInstance().font;
    }

    public HollowWidget(ITextComponent name) {
        this(0, 0, 0, 0, name);
    }

    @Override
    public void renderButton(MatrixStack stack, int mouseX, int mouseY, float ticks) {
        for (Widget widget : widgets) {
            widget.setFocused(mouseX >= widget.x && mouseY >= widget.y && mouseX < widget.x + widget.getWidth() && mouseY < widget.y + widget.getHeight());
            widget.render(stack, mouseX, mouseY, ticks);
        }
    }

    public void init() {
    }

    public <T extends Widget> T addWidget(T widget) {
        this.widgets.add(widget);
        return widget;
    }

    public void addWidgets(Widget... widgets) {
        this.widgets.addAll(Arrays.asList(widgets));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (Widget widget : widgets) {
            if (widget.isHovered()) {
                return widget.mouseClicked(mouseX, mouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Widget widget : widgets) {
            widget.mouseReleased(mouseX, mouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        for (Widget widget : widgets) {
            if (widget.isHovered()) return widget.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scroll) {
        for (Widget widget : widgets) {
            if (widget.isHovered()) return widget.mouseScrolled(mouseX, mouseX, scroll);
        }
        return super.mouseScrolled(mouseX, mouseY, scroll);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        for (Widget widget : widgets) {
            if (widget.isHovered()) widget.mouseMoved(mouseX, mouseX);
        }
        super.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (Widget widget : widgets) {
            if (widget.isHovered()) return widget.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (Widget widget : widgets) {
            if (widget.isHovered()) return widget.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char character, int p_231042_2_) {
        for (Widget widget : widgets) {
            if (widget.isHovered()) return widget.charTyped(character, p_231042_2_);
        }
        return super.charTyped(character, p_231042_2_);
    }

    public void bind(String modid, String path) {
        this.textureManager.bind(new ResourceLocation(modid, "textures/" + path));
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
                getAlignmentPosX(alignment, offsetX + this.x, this.width, targetWidth, size),
                getAlignmentPosY(alignment, offsetY - this.y, this.height, targetHeight, size),
                texX, texY, (int) (targetWidth * size), (int) (targetHeight * size), (int) (imageWidth * size), (int) (imageHeight * size)
        );
    }

    public void setX(int x) {
        int lx = this.x;
        this.x = x;
        for (Widget widget : widgets) {
            if (widget instanceof HollowWidget) {
                ((HollowWidget) widget).setX(widget.x - lx + x);
            } else {
                widget.x = widget.x - lx + x;
            }
        }
    }

    public void setY(int y) {
        int ly = this.y;
        this.y = y;
        for (Widget widget : widgets) {
            if (widget instanceof HollowWidget) {
                ((HollowWidget) widget).setY(widget.y - ly + y);
            } else {
                widget.y = widget.y - ly + y;
            }
        }
    }
}
