package ru.hollowhorizon.hc.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class OpenGLUtils {
    public static void drawLine(BufferBuilder bufferbuilder, Matrix4f matrix, Vector3d from, Vector3d to, float r, float g, float b, float a) {
        bufferbuilder.vertex(matrix, (float) from.x(), (float) from.y() - 0.1F, (float) from.z()).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix, (float) to.x(), (float) to.y() - 0.1F, (float) to.z()).color(r, g, b, a).endVertex();
    }
}

