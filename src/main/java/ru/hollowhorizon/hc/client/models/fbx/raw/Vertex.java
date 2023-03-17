package ru.hollowhorizon.hc.client.models.fbx.raw;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import ru.hollowhorizon.hc.client.models.fbx.FBXBone;
import ru.hollowhorizon.hc.client.models.fbx.FBXSkeleton;

import java.util.List;

public class Vertex {
    public float x, y, z, nx, ny, nz, u, v;
    private int index;

    public Vertex(int index, float x, float y, float z, float nx, float ny, float nz, float u, float v) {
        this.index = index;
        this.x = x;
        this.y = y;
        this.z = z;
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.u = u;
        this.v = v;
    }

    public int getIndex() {
        return index;
    }

    public void draw(FBXSkeleton skeleton, Matrix4f posMat, Matrix3f norMat, IVertexBuilder builder, int light, int overlay) {
        Vector4f pos = new Vector4f(x, y, z, 1.0F);
        List<FBXBone> bones = skeleton.getBones();

        for (FBXBone bone : bones) {

        }

        pos.transform(posMat);

        Vector3f nor = new Vector3f(nx, ny, nz);
        nor.transform(norMat);

        builder.vertex(pos.x(), pos.y(), pos.z(),
                1, 1, 1, 0.3F,
                u, v,
                overlay,
                light,
                nor.x(), nor.y(), nor.z());

    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getNX() {
        return nx;
    }

    public float getNY() {
        return ny;
    }

    public float getNZ() {
        return nz;
    }

    public float getU() {
        return u;
    }

    public float getV() {
        return v;
    }

    @Override
    public String toString() {
        return "v: " + x + " " + y + " " + z +
                " n: " + nx + " " + ny + " " + nz
                + " uv: " + u + " " + v;
    }

    public void transform(Matrix4f matrix, Float value) {

    }
}
