package ru.hollowhorizon.hc.client.models.fbx;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXElement;
import ru.hollowhorizon.hc.client.models.fbx.raw.FBXFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FBXSkeleton {
    private final FBXBone root;
    private final FBXFile file;

    public FBXSkeleton(FBXFile file, FBXBone root) {
        this.root = root;
        this.file = file;

        createPoseBinding(root);


        this.root.calculateIMSBT(new Matrix4f());
    }

    private void printBone(FBXBone root) {

        System.out.println(root.getName()+": ");
        Vector4f v = new Vector4f(1F, 1F, 1F, 0F);

        System.out.println(printM(root.getLocalMatrix()));


        System.out.println(v.x()+" "+v.y()+" "+v.z());
        for (FBXBone child : root.getChildren()) {
            printBone(child);
        }
    }

    public Matrix4f calculateMatrixTransform(FBXBone bone) {
        if (bone.getParent() == null) {
            return bone.modified.copy();
        } else {
            Matrix4f m = bone.modified.copy();
            m.multiply(calculateMatrixTransform(bone.getParent()));
            return m;
        }
    }

    public static Matrix4f createFuckingFBXMatrix(float[] matrix) {
        Matrix4f m = new Matrix4f();
        m.m00 = matrix[0];
        m.m01 = matrix[4];
        m.m02 = matrix[8];
        m.m03 = matrix[12];
        m.m10 = matrix[1];
        m.m11 = matrix[5];
        m.m12 = matrix[9];
        m.m13 = matrix[13];
        m.m20 = matrix[2];
        m.m21 = matrix[6];
        m.m22 = matrix[10];
        m.m23 = matrix[14];
        m.m30 = matrix[3];
        m.m31 = matrix[7];
        m.m32 = matrix[11];
        m.m33 = matrix[15];
        return m;
    }

    private void createPoseBinding(FBXBone bone) {
        FBXElement pose = file.getElement("Objects").getFirstElement("Pose");

        List<FBXElement> poseNodes = pose.getElements("PoseNode");

        for (FBXElement poseNodeElement : poseNodes) {
            long nodeId = poseNodeElement.getFirstElement("Node").getProperties()[0].getData();
            if (nodeId != bone.getId()) {
                continue;
            }

            float[] matrix = FBXBone.toFloatArray(poseNodeElement.getFirstElement("Matrix").getProperties()[0].getData());

            Matrix4f bindTransform = new Matrix4f(matrix);
            bindTransform.transpose();

            bone.setLocalTransform(bindTransform);

            bone.getChildren().forEach(this::createPoseBinding);
        }

    }

    private String printM(Matrix4f m) {
        return m.m00 + "		" + m.m01 + "		" + m.m02 + "		" + m.m03 + "\n" +
                m.m10 + "		" + m.m11 + "		" + m.m12 + "		" + m.m13 + "\n" +
                m.m20 + "		" + m.m21 + "		" + m.m22 + "		" + m.m23 + "\n" +
                m.m30 + "		" + m.m31 + "		" + m.m32 + "		" + m.m33 + "\n";
    }

    public FBXBone getRoot() {
        return root;
    }

    public Vector4f applyTransform(Vector4f vector4f, int index) {
        return root.applyTransform(vector4f, index);
    }

    public FBXBone getBone(String name) {
        return getBone(this.root, name);
    }

    public FBXBone getBone(FBXBone bone, String s) {
        if (bone.getName().equals(s)) return bone;

        for (FBXBone child : bone.getChildren()) {
            if (child.getName().equals(s)) return child;

            FBXBone result = getBone(child, s);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public void update(MatrixStack stack) {
        //this.root.updatePose(MatrixUtils.IDENTITY.copy(), MatrixUtils.IDENTITY.copy());


    }

    public List<FBXBone> getBones() {
        List<FBXBone> bones = new ArrayList<>();
        addBones(bones, this.root);
        return bones;
    }

    public void addBones(List<FBXBone> bones, FBXBone bone) {
        bones.add(bone);
        bone.getChildren().forEach(child -> addBones(bones, child));
    }

    public void print() {
        printBone(this.root);
    }
}
