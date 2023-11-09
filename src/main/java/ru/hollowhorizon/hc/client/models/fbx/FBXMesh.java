package ru.hollowhorizon.hc.client.models.fbx;

import java.util.ArrayList;
import java.util.List;

public class FBXMesh {
    public final int mode;
    private final FBXVertex[] vertices;
    private final float[] normals;
    private final float[] uvMap;
    private final int indCount;
    private final int[] indices;
    private final long modelId;
    private final List<FBXCurveNode> animationData = new ArrayList<>();

    public FBXMesh(long modelId, double[] vertices, double[] normals, double[] uvMap, int[] indices, int mode) {
        this.vertices = FBXVertex.fromArray(vertices);
        this.normals = toFloatArray(normals);
        this.uvMap = toFloatArray(uvMap);
        this.indCount = indices.length;
        this.indices = indices;
        this.mode = mode;
        this.modelId = modelId;
    }

    public void addAnimationData(FBXCurveNode node) {
        this.animationData.add(node);
    }

    private float[] toFloatArray(double[] array) {
        int size = array.length;
        float[] floats = new float[size];
        for (int i = 0; i < size; i++) {
            floats[i] = (float) array[i];
        }
        return floats;
    }

    public void clearAnimations() {
        this.animationData.clear();
    }

    public long getModelId() {
        return modelId;
    }
}
