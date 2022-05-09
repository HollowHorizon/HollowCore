package ru.hollowhorizon.hc.client.render.shaders;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import org.lwjgl.opengl.GL11;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;

public class PostShaderRenderer {

    private static Framebuffer framebuffer;

    public static void renderPostShader(ShaderProgram program) {
        Framebuffer minecraftBuffer = Minecraft.getInstance().getMainRenderTarget();
        if(framebuffer==null) {
            framebuffer = new Framebuffer(minecraftBuffer.width, minecraftBuffer.height, true, Minecraft.ON_OSX);
            framebuffer.clear(Minecraft.ON_OSX);
        }
        //processShader(minecraftBuffer, framebuffer, program);
        processShader(minecraftBuffer, program);
    }

    private static void processShader(Framebuffer target, ShaderProgram program) {
        float width = (float)target.width;
        float height = (float)target.height;

        RenderSystem.viewport(0, 0, (int)width, (int)height);

        program.use();

        target.bindWrite(false);

        RenderSystem.depthFunc(519);

        GL11.glBindTexture(GL_TEXTURE_2D, target.getColorTextureId());

        //считываем область из буффера 1 в буффер 2
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(0.0D, 0.0D, 500.0D).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(width, 0.0D, 500.0D).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(width, height, 500.0D).color(255, 255, 255, 255).endVertex();
        bufferbuilder.vertex(0.0D, height, 500.0D).color(255, 255, 255, 255).endVertex();
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);

        RenderSystem.depthFunc(515);
        program.release();
        target.unbindWrite();
    }
}
