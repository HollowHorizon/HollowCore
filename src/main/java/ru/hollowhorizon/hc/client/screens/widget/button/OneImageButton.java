package ru.hollowhorizon.hc.client.screens.widget.button;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.ExtendedButton;
import org.lwjgl.opengl.GL11;

public class OneImageButton extends ExtendedButton {
    private final ResourceLocation texture;

    public OneImageButton(int xPos, int yPos, int width, int height, ITextComponent displayString, IPressable handler, ResourceLocation texture) {
        super(xPos, yPos, width, height, displayString, handler);
        this.texture = texture;
    }

    public OneImageButton(int xPos, int yPos, int width, int height, IPressable handler, ResourceLocation texture) {
        this(xPos, yPos, width, height, new StringTextComponent(""), handler, texture);
    }

    @Override
    protected void renderBg(MatrixStack p_230441_1_, Minecraft p_230441_2_, int p_230441_3_, int p_230441_4_) {
        Minecraft.getInstance().textureManager.bind(texture);
        float color = isHovered ? 0.7F : 1.0F;
        GL11.glColor4f(color, color, color, 1F);
        blit(p_230441_1_, this.x, this.y, 0, 0, width, height, width, height);
        GL11.glColor4f(1F, 1F, 1F, 1F);
    }
}
