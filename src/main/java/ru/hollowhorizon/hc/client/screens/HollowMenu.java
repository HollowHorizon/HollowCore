package ru.hollowhorizon.hc.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

import javax.annotation.Nonnull;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class HollowMenu extends Screen {
    private static final ResourceLocation LOGO = new ResourceLocation(MODID, "textures/hollow_core_logo.png");

    public HollowMenu() {
        super(new StringTextComponent("HOLLOW_MAIN_MENU"));
    }

    @Override
    public void render(@Nonnull MatrixStack matrixStack, int mouseX, int mouseY, float ticks) {
        super.render(matrixStack, mouseX, mouseY, ticks);
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        renderBackground(matrixStack);
        tm.bind(LOGO);
        blit(matrixStack, this.width / 4, 20, 0, 0, this.width / 2, this.height / 3, this.width / 2, this.height / 3);
    }
}
