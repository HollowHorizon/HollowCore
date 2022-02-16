package ru.hollowhorizon.hc.client.model.fbx;

import java.util.Arrays;

public class FBXVertex {
    public final float x;
    public final float y;
    public final float z;

    public FBXVertex(double x, double y, double z) {
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
    }

    public static FBXVertex[] fromArray(double[] vertices) {
        int l = vertices.length / 3;
        FBXVertex[] array = new FBXVertex[l];
        for (int i = 0; i < l; i++) {
            int ind = i * 3;
            array[i] = new FBXVertex(vertices[ind], vertices[ind + 1], vertices[ind + 2]);
        }
        return array;
    }

    @Override
    public String toString() {
        return x + ":" + y + ":" + z;
    }
}
