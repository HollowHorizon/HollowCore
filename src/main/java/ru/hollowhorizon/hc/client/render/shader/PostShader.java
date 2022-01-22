package ru.hollowhorizon.hc.client.render.shader;

import com.google.gson.JsonSyntaxException;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderInstance;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;

public class PostShader extends ShaderGroup {
    private final int width;
    private final int height;

    public PostShader(TextureManager texManager, IResourceManager resManager, Framebuffer target, ResourceLocation name) throws JsonSyntaxException, IOException {
        super(texManager, resManager, target, name);
        this.width = target.viewWidth;
        this.height = target.viewHeight;
    }

    public PostShader(String domain, String name) throws JsonSyntaxException, IOException {
        this(Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getResourceManager(), Minecraft.getInstance().getMainRenderTarget(), new ResourceLocation(domain, "shaders/post/" + name + ".json"));
        this.resize(Minecraft.getInstance().getWindow().getWidth(), Minecraft.getInstance().getWindow().getHeight());
    }

    public ShaderInstance getMainShader() {
        return this.passes.get(0).getEffect();
    }

    @Override
    public void process(float frameTime) {
        MainWindow w = Minecraft.getInstance().getWindow();
        if (this.width != w.getWidth() || this.height != w.getHeight())
            this.resize(w.getWidth(), w.getHeight());
        super.process(frameTime);
    }
}
