package ru.hollowhorizon.hc.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.util.InputMappings;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;


import javax.annotation.Nonnull;

import java.util.ArrayList;
import java.util.List;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class EventListScreen extends Screen {
    private final List<String> list = new ArrayList<>();
    private final ResourceLocation EVENT_LIST_TOP = new ResourceLocation(MODID, "textures/gui/event_list/event_list_top.png");
    private final ResourceLocation EVENT_STRING = new ResourceLocation(MODID, "textures/gui/event_list/event_list_string.png");
    private final int lastGuiScale = Minecraft.getInstance().options.guiScale;
    private int scroller = 0;

    public EventListScreen(List<String> list) {
        super(new StringTextComponent("EVENT_LIST"));
        Minecraft.getInstance().options.guiScale = 4;
        Minecraft.getInstance().options.hideGui = true;
        Minecraft.getInstance().resizeDisplay();
        initData(list);
    }

    public void initData(List<String> list) {
        for(String data : list) {
            TranslationTextComponent text = new TranslationTextComponent(data+".desc");
            if(!text.getString().equals("{hide}")) {
                this.list.add(data);
            }
        }
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(@Nonnull MatrixStack stack, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(stack, p_230430_2_, p_230430_3_, p_230430_4_);
        renderBackground(stack);
        for (int i = 0; i < this.list.size(); i++) {
            renderEvent(stack, new TranslationTextComponent(this.list.get(i)+".desc"), i);
        }

        renderTop(stack);
    }

    public void renderTop(MatrixStack stack) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        tm.bind(EVENT_LIST_TOP);
        blit(stack, 0, 0, 0, 0, this.width, this.height / 10, this.width, this.height / 10);
        drawCentredStringNoShadow(stack, font, new TranslationTextComponent("hc.gui.event_list"), this.width / 2, 7, 0xFFFFFF);
    }

    public void renderEvent(MatrixStack stack, TextComponent description, int id) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        TextureManager tm = Minecraft.getInstance().getTextureManager();
        tm.bind(EVENT_STRING);
        blit(stack, 0, (this.height / 20 + 10) * id + this.height / 7 - scroller, 0, 0, this.width, this.height / 16, this.width, this.height / 16);
        drawStringNoShadow(stack, font, description, 5, (this.height / 20 + 10) * id + this.height / 7 + 3 - scroller, 0xFFFFFF);
    }

    public void closeScreen() {
        Minecraft.getInstance().options.guiScale = lastGuiScale;
        Minecraft.getInstance().setScreen(null);
        Minecraft.getInstance().options.hideGui = false;
        Minecraft.getInstance().resizeDisplay();
    }

    public boolean keyPressed(int key, int scan, int param3) {
        if (super.keyPressed(key, scan, param3)) {
            return true;
        } else {
            if (key == 87) {
                changeScroller(1);
            } else if (key == 83) {
                changeScroller(-1);
            }
            if (this.isExitKey(key, scan)) {
                closeScreen();
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    protected boolean isExitKey(int key, int scan) {
        return key == 256 || Minecraft.getInstance().options.keyInventory.isActiveAndMatches(InputMappings.getKey(key, scan));
    }

    @Override
    public boolean mouseScrolled(double value1, double value2, double value3) {
        changeScroller((int) value3);

        return false;
    }

    public void changeScroller(int value) {
        int lastHeight = (this.height / 20 + 10) * 2 + this.height / 7 + this.height / 16;
        if (scroller - value * 5 > 0 && scroller - value * 5 < lastHeight - this.height + 15) scroller -= value * 5;
    }

    protected void drawStringNoShadow(MatrixStack stack, FontRenderer fr, TextComponent text, int x, int y, int color) {
        fr.draw(stack, text, (float) (x), (float) y, color);
    }

    protected void drawCentredStringNoShadow(MatrixStack stack, FontRenderer fr, ITextComponent text, int x, int y, int color) {
        IReorderingProcessor ireorderingprocessor = text.getVisualOrderText();
        fr.draw(stack, ireorderingprocessor, (float) (x - fr.width(ireorderingprocessor) / 2), (float) y, color);
    }
}
