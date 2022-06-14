package ru.hollowhorizon.hc.client.screens;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import ru.hollowhorizon.hc.client.video.DynamicResourceLocation;
import ru.hollowhorizon.hc.client.video.SimpleMediaPlayer;
import ru.hollowhorizon.hc.common.registry.VideoRegistry;

import java.io.*;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class VideoScreen extends Screen {
    private final SimpleMediaPlayer player;
    private final TextureManager textureManager;
    private boolean init;

    public VideoScreen() {
        super(new StringTextComponent(""));

        this.player = new SimpleMediaPlayer(new DynamicResourceLocation(MODID, "player"));
        this.textureManager = Minecraft.getInstance().textureManager;
    }

    @Override
    protected void init() {
        super.init();

        String link = "";
        File file = new File("C:\\Users\\user\\Desktop\\v.txt");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            link = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.player.api().media().prepare(VideoRegistry.VIDEOS.getValue(new ResourceLocation(MODID, "videos/logo_test.mp4")).getMedia());
        this.player.api().controls().setRepeat(true);

        this.player.api().audio().setVolume(200);

        init = false;
    }

    @Override
    public void render(MatrixStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_) {
        super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);

        if (!init) {
            this.player.api().controls().play();
            init = true;
        }


        this.player.renderToResourceLocation();
        this.player.dynamicTexture.bind();

        RenderSystem.enableBlend();
        blit(p_230430_1_, 0, 0, 0, 0, this.width, this.height, this.width, this.height);
        RenderSystem.disableBlend();
    }

    @Override
    public void onClose() {
        super.onClose();
        this.player.cleanup();
    }
}