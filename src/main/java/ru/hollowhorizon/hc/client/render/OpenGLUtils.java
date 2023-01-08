package ru.hollowhorizon.hc.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

public class OpenGLUtils {
    public static void drawLine(BufferBuilder bufferbuilder, Matrix4f matrix, Vector3d from, Vector3d to, float r, float g, float b, float a) {
        bufferbuilder.vertex(matrix, (float) from.x(), (float) from.y() - 0.1F, (float) from.z()).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix, (float) to.x(), (float) to.y() - 0.1F, (float) to.z()).color(r, g, b, a).endVertex();
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

