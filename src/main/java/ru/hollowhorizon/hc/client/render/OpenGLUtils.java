package ru.hollowhorizon.hc.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3d;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLCapabilities;

public class OpenGLUtils {
    public static boolean isSupportedGL43;

    public static void init() {
        RenderSystem.recordRenderCall(() -> {
            GLCapabilities caps = GL.getCapabilities();
            isSupportedGL43 = caps.OpenGL43;
        });
    }
    public static void drawLine(BufferBuilder bufferbuilder, Matrix4f matrix, Vector3d from, Vector3d to, float r, float g, float b, float a) {
        bufferbuilder
                .vertex(matrix, (float) from.x, (float) from.y - 0.1F, (float) from.z)
                .color(r, g, b, a)
                .endVertex();
        bufferbuilder
                .vertex(matrix, (float) to.x, (float) to.y - 0.1F, (float) to.z)
                .color(r, g, b, a)
                .endVertex();
    }

    public static void drawRect(int x, int y, int width, int height, int zLayer) {
        GL11.glPushMatrix();

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(0, 0);
        GL11.glVertex3f(x, y, zLayer);
        GL11.glTexCoord2f(0, 1);
        GL11.glVertex3f(x, y + height + 0f, zLayer);
        GL11.glTexCoord2f(1, 1);
        GL11.glVertex3f(x + width + 0f, y + height + 0f, zLayer);
        GL11.glTexCoord2f(1, 0);
        GL11.glVertex3f(x + width + 0f, y, zLayer);
        GL11.glEnd();

        GL11.glPopMatrix();
    }
}

