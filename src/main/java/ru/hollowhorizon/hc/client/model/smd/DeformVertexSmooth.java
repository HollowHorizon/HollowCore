package ru.hollowhorizon.hc.client.model.smd;

public class DeformVertexSmooth extends DeformVertex {
    private boolean init = false;
    private float nextx;
    private float nexty;
    private float nextz;
    private float nextxn;
    private float nextyn;
    private float nextzn;

    public DeformVertexSmooth(float x, float y, float z, float xn, float yn, float zn, int ID) {
        super(x, y, z, xn, yn, zn, ID);
    }

    public void applyChange() {
        if (this.init) {
            this.x = this.nextx;
            this.y = this.nexty;
            this.z = this.nextz;
        }

        if (this.currentLocMod == null) {
            this.nextx = this.baseLoc.x();
            this.nexty = this.baseLoc.y();
            this.nextz = this.baseLoc.z();
        } else {
            this.nextx = this.currentLocMod.x();
            this.nexty = this.currentLocMod.y();
            this.nextz = this.currentLocMod.z();
        }

        if (this.currentNormalMod == null) {
            this.nextxn = this.baseNormal.x();
            this.nextyn = this.baseNormal.y();
            this.nextzn = this.baseNormal.z();
        } else {
            this.nextxn = this.currentNormalMod.x();
            this.nextyn = this.currentNormalMod.y();
            this.nextzn = this.currentNormalMod.z();
        }

        if (!this.init) {
            this.x = this.nextx;
            this.y = this.nexty;
            this.z = this.nextz;
            this.init = true;
        }

    }

    public float getX(float partialTick) {
        return this.x * (1.0F - partialTick) + this.nextx * partialTick;
    }

    public float getY(float partialTick) {
        return this.y * (1.0F - partialTick) + this.nexty * partialTick;
    }

    public float getZ(float partialTick) {
        return this.z * (1.0F - partialTick) + this.nextz * partialTick;
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