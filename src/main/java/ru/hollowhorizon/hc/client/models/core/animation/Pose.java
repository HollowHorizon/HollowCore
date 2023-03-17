package ru.hollowhorizon.hc.client.models.core.animation;

import ru.hollowhorizon.hc.client.utils.math.Matrix4d;

import static ru.hollowhorizon.hc.client.models.core.animation.AnimationFrame.MAX_JOINTS;

public class Pose implements IPose {

    private final Matrix4d[] jointMatrices;
    private int jointCount;

    public Pose() {
        jointMatrices = new Matrix4d[MAX_JOINTS];
        for (int i = 0; i < MAX_JOINTS; i++) {
            jointMatrices[i] = new Matrix4d();
        }
    }

    @Override
    public Matrix4d[] getJointMatrices() {
        return jointMatrices;
    }

    @Override
    public Matrix4d getJointMatrix(int index) {
        return jointMatrices[index];
    }

    @Override
    public int getJointCount() {
        return jointCount;
    }

    @Override
    public void setJointCount(int count) {
        jointCount = count;
    }

    @Override
    public void setJointMatrix(int index, Matrix4d mat) {
        jointMatrices[index].set(mat);
    }
}
