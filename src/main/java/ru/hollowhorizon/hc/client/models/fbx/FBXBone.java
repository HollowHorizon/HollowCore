package ru.hollowhorizon.hc.client.models.fbx;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXElement;
import ru.hollowhorizon.hc.client.utils.math.MatrixUtils;

import java.util.*;

public class FBXBone {
    private final long id;
    private final String name;
    private final List<FBXBone> children = new ArrayList<>();

    public final List<Integer> indexes = new ArrayList<>();
    public final List<Float> weihts = new ArrayList<>();

    public Matrix4f modified = new Matrix4f(new float[]{1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F, 0F, 0F, 0F, 0F, 1F});
    private FBXBone parent;
    private Matrix4f localTransform;

    public FBXBone(String pName, long id) {
        this.name = pName;
        this.id = id;
    }

    public static float[] toFloatArray(double[] array) {
        float[] result = new float[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = (float) array[i];
        }
        return result;
    }

    private void toMap(int[] indexes, float[] weights) {
        for (int i = 0; i < indexes.length; i++) {
            this.indexes.add(indexes[i]);
            this.weihts.add(weights[i]);
        }
    }

    public void addChild(FBXBone child) {
        children.add(child);
    }

    public long getId() {
        return id;
    }

    public FBXBone getParent() {
        return parent;
    }

    public void setParent(FBXBone parent) {
        this.parent = parent;
    }

    public List<FBXBone> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return this.getName() + ": " + Arrays.toString(this.getChildren().toArray());
    }

    public void applyDeformer(FBXElement element) {
        if (element.hasElement("Indexes")) {
            int[] indexes = element.getFirstElement("Indexes").getProperties()[0].getData();
            float[] weights = toFloatArray(element.getFirstElement("Weights").getProperties()[0].getData());



            toMap(indexes, weights);
        }
    }

    public Vector4f applyTransform(Vector4f vector, int index) {


        for (FBXBone child : this.getChildren()) {
            vector = child.applyTransform(vector, index);
        }

        return vector;
    }

    public void setLocalTransform(Matrix4f bindTransform) {
        this.localTransform = bindTransform;
    }

    public void calculateIMSBT(Matrix4f parentTransform) {
        final Matrix4f msbt = MatrixUtils.mul(parentTransform, this.getLocalMatrix(), null);

        children.forEach(child -> child.calculateIMSBT(msbt));
    }

    public void rotate(int i, int i1, int i2, int i3) {
        //this.transform.multiply(Vector3f.XN.rotation(i));
    }

    public Matrix4f getLocalMatrix() {
        if (localTransform == null) {
            localTransform = new Matrix4f();
            localTransform.setIdentity();
        }

        return this.localTransform;
    }

    public void scale(float s) {
        this.localTransform.multiply(Matrix4f.createScaleMatrix(s, s, s));
    }

    public void translate(float v) {
        this.localTransform.multiply(Matrix4f.createTranslateMatrix(0F, v, 0F));
    }
}
