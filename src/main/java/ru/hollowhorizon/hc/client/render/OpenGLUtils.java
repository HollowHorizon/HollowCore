package ru.hollowhorizon.hc.client.render;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class OpenGLUtils {
    //параметры rgba - цвет
    public static void drawLine(BufferBuilder bufferbuilder, Matrix4f matrix, Vector3d from, Vector3d to, float r, float g, float b, float a) {
        //указываем все точки, режим у нас lines, так что каждые 2 точки будут соединены в одну линию, если выбрать режим треугольников, то каждые 3 точки образуют треугольник
        //если указан режим quad, то соответственно рисуются квадраты через каждые 4 точки
        //собственно аргументы: матрица нужна, чтобы указать где находятся координаты в мире, т.е. относительно чего считать начало координат, дальше собственоо сами точки и цвет
        bufferbuilder.vertex(matrix, (float) from.x(), (float) from.y() - 0.1F, (float) from.z()).color(r, g, b, a).endVertex();
        bufferbuilder.vertex(matrix, (float) to.x(), (float) to.y() - 0.1F, (float) to.z()).color(r, g, b, a).endVertex();
    }
}

