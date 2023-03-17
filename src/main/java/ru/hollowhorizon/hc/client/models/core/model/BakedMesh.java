package ru.hollowhorizon.hc.client.models.core.model;

import java.util.List;

/**
 * Created by Jacob on 1/25/2020.
 */
public class BakedMesh {

    public final float[] positions;
    public final float[] texCoords;
    public final float[] normals;
    public final int[] indices;
    public final int vertices;
    public String name;

    public BakedMesh(String name, float[] positions, float[] texCoords, float[] normals, int[] indices){
        this.positions = positions;
        this.texCoords = texCoords;
        this.normals = normals;
        this.indices = indices;
        this.name = name;
        this.vertices = positions.length / 3;
    }

    public BakedMesh(String name, List<Float> positions, List<Float> texCoords, List<Float> normals, List<Integer> indices){
        this.positions = toFloatArray(positions);
        this.texCoords = toFloatArray(texCoords);
        this.normals = toFloatArray(normals);
        this.indices = toIntArray(indices);
        this.name = name;
        this.vertices = positions.size() / 3;
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
