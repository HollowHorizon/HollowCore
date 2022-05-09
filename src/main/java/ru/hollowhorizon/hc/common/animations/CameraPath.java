package ru.hollowhorizon.hc.common.animations;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import ru.hollowhorizon.hc.client.render.OpenGLUtils;
import ru.hollowhorizon.hc.client.utils.math.HollowInterpolation;

import java.util.ArrayList;
import java.util.List;

public class CameraPath {
    private final Vector3d fromPos;
    private final Vector3d toPos;
    private final List<Vector3d> precalculatedPath = new ArrayList<>();
    private HollowInterpolation interpolation = HollowInterpolation.LINEAR;

    public CameraPath(Vector3d fromPos, Vector3d toPos) {
        this.fromPos = fromPos;
        this.toPos = toPos;
        calculatePath(100);
    }

    public HollowInterpolation getInterpolation() {
        return interpolation;
    }

    public void setInterpolation(HollowInterpolation interpolation) {
        this.interpolation = interpolation;
        calculatePath(100);
    }

    public void setPosCount(int posCount) {
        calculatePath(posCount);
    }

    public Vector3d getFromPos() {
        return fromPos;
    }

    public Vector3d getToPos() {
        return toPos;
    }

    public void drawPath(MatrixStack matrixStack) {
        //вызываем "рисовалку"
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuilder();

        //тип рисования линии (это значит каждые 2 точки будут превращены в линию)
        bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        //сохраняем начальную позицию матрицы, т.е. место, где она была до переноса
        matrixStack.pushPose();

        //переносим матрицу на место игрока
        Vector3d projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        matrixStack.translate(-projectedView.x, -projectedView.y, -projectedView.z);

        //толщина линии
        GL11.glLineWidth(4);

        //достаём саму матрицу
        Matrix4f matrix = matrixStack.last().pose();
        int size = precalculatedPath.size();
        for (int i = 1; i < size; i++) {
            //тут собственно указание самих точек, я это добавил в функцию, щас распишу подробнее
            OpenGLUtils.drawLine(bufferbuilder, matrix, precalculatedPath.get(i), precalculatedPath.get(i - 1), 1.0F, 1.0F, 1.0F, 0.5F);
        }
        //завершаем рисование
        tessellator.end();

        //меняем толщину линии обратно
        GL11.glLineWidth(1);
        //возвращаем начальную позицию, т.к. мы меняли её положение
        matrixStack.popPose();
    }

    public void calculatePath(int posCount) {
        precalculatedPath.clear();
        for (float i = 0; i < posCount; i++) {
            precalculatedPath.add(getPosByKey(i/posCount));
        }
    }

    public Vector3d getPosByFrame(int frame) {
        return precalculatedPath.get(frame);
    }

    private Vector3d getPosByKey(float key) {
        double posX = interpolation.interpolate(fromPos.x(), toPos.x(), key);
        double posY = interpolation.interpolate(fromPos.y(), toPos.y(), key);
        double posZ = interpolation.interpolate(fromPos.z(), toPos.z(), key);
        return new Vector3d(posX, posY, posZ);
    }
}
