package ru.hollowhorizon.hc.client.models.core.model;


import java.util.List;

public class BakedAnimatedMesh extends BakedMesh {
    public final float[] weights;
    public final int[] boneIds;

    public BakedAnimatedMesh(String name, float[] positions, float[] texCoords,
                             float[] normals, int[] indices, float[] weights, int[] boneIds) {
        super(name, positions, texCoords, normals, indices);
        this.weights = weights;
        this.boneIds = boneIds;
    }

    public BakedAnimatedMesh(String name, List<Float> positions, List<Float> texCoords,
                             List<Float> normals, List<Integer> indices, List<Float> weights, List<Integer> boneIds) {
        super(name, positions, texCoords, normals, indices);
        this.weights = toFloatArray(weights);
        this.boneIds = toIntArray(boneIds);
    }

    private float[] toFloatArray(List<Float> list){
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    private int[] toIntArray(List<Integer> list){
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
