package ru.hollowhorizon.hc.client.model.smd;

import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector4f;
import ru.hollowhorizon.hc.client.utils.math.MatrixUtils;
import ru.hollowhorizon.hc.client.utils.math.VectorUtils;

public class DeformVertex extends Vertex {
    public DeformVertex copy = null;
    protected final Vector4f baseLoc;
    public Vector4f currentLocMod;
    protected final Vector4f baseNormal;
    public Vector4f currentNormalMod;
    public Vector4f storage = new Vector4f();
    public Vector4f storageNorm = new Vector4f();
    private boolean reset = false;
    public final int ID;
    public float xn;
    public float yn;
    public float zn;

    public DeformVertex(DeformVertex vertex) {
        super(vertex.x, vertex.y, vertex.z);
        this.xn = vertex.xn;
        this.yn = vertex.yn;
        this.zn = vertex.zn;
        this.baseLoc = new Vector4f();
        this.baseLoc.set(vertex.baseLoc.x(), vertex.baseLoc.y(), vertex.baseLoc.z(), vertex.baseLoc.w());
        this.baseNormal = new Vector4f();
        this.baseLoc.set(vertex.baseNormal.x(), vertex.baseNormal.y(), vertex.baseNormal.z(), vertex.baseNormal.w());
        this.ID = vertex.ID;
        vertex.copy = this;
        this.currentLocMod = new Vector4f();
        this.currentLocMod.set(vertex.currentLocMod.x(), vertex.currentLocMod.y(), vertex.currentLocMod.z(), vertex.currentLocMod.w());
        this.currentNormalMod = new Vector4f();
        this.currentLocMod.set(vertex.currentNormalMod.x(), vertex.currentNormalMod.y(), vertex.currentNormalMod.z(), vertex.currentNormalMod.w());
    }

    public DeformVertex(float x, float y, float z, float xn, float yn, float zn, int ID) {
        super(x, y, z);
        this.xn = xn;
        this.yn = yn;
        this.zn = zn;
        this.baseLoc = new Vector4f(x, y, z, 1.0F);
        this.baseNormal = new Vector4f(xn, yn, zn, 0.0F);
        this.currentLocMod = new Vector4f(20.0F, 20.0F, 20.0F, 1.0F);
        this.currentNormalMod = new Vector4f(20.0F, 20.0F, 20.0F, 0.0F);
        this.ID = ID;
    }

    public void reset() {
        this.reset = true;
    }

    protected void initModVectors() {
        if (this.currentLocMod == null) {
            this.currentLocMod = new Vector4f();
        }

        if (this.currentNormalMod == null) {
            this.currentNormalMod = new Vector4f();
        }

    }

    public void applyModified(Bone bone, float weight) {
        Matrix4f modified = bone.modified;
        if (modified != null) {
            if (this.reset) {
                this.currentLocMod.set(0.0F, 0.0F, 0.0F, 0.0F);
                this.currentNormalMod.set(0.0F, 0.0F, 0.0F, 0.0F);
                this.reset = false;
            }

            this.storage.set(0.0F, 0.0F, 0.0F, 0.0F);
            this.storageNorm.set(0.0F, 0.0F, 0.0F, 0.0F);
            MatrixUtils.transform(modified, this.baseLoc, this.storage);
            MatrixUtils.transform(modified, this.baseLoc, this.storage);
            MatrixUtils.transform(modified, this.baseNormal, this.storageNorm);

            VectorUtils.scale(this.storage, weight);
            VectorUtils.scale(this.storageNorm, weight);
            VectorUtils.add(this.storage, this.currentLocMod, this.currentLocMod);
            VectorUtils.add(this.storageNorm, this.currentNormalMod, this.currentNormalMod);
        }

    }

    public void applyChange() {
        if (this.reset) {
            this.x = this.baseLoc.x();
            this.y = this.baseLoc.y();
            this.z = this.baseLoc.z();
        } else {
            this.x = this.currentLocMod.x();
            this.y = this.currentLocMod.y();
            this.z = this.currentLocMod.z();
        }

        if (this.reset) {
            this.xn = this.baseNormal.x();
            this.yn = this.baseNormal.y();
            this.zn = this.baseNormal.z();
        } else {
            this.xn = this.currentNormalMod.x();
            this.yn = this.currentNormalMod.y();
            this.zn = this.currentNormalMod.z();
        }

    }

    public boolean equals(float x, float y, float z) {
        return this.x == x && this.y == y && this.z == z;
    }

    public float getX(float partialTick) {
        return this.x;
    }

    public float getY(float partialTick) {
        return this.y;
    }

    public float getZ(float partialTick) {
        return this.z;
    }

    public float getXN(float partialTick) {
        return this.xn;
    }

    public float getYN(float partialTick) {
        return this.yn;
    }

    public float getZN(float partialTick) {
        return this.zn;
    }
}
