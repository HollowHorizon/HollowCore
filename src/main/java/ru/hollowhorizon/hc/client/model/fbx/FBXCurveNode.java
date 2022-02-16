package ru.hollowhorizon.hc.client.model.fbx;

import net.minecraft.util.math.vector.Vector3f;

public class FBXCurveNode {
    private final FBXKeyFrame x;
    private final FBXKeyFrame y;
    private final FBXKeyFrame z;
    private final CurveType type;
    private final long modelId;
    private float currentX;
    private float currentY;
    private float currentZ;

    public FBXCurveNode(FBXKeyFrame x, FBXKeyFrame y, FBXKeyFrame z, CurveType type, long modelId) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;

        switch (type) {
            case SCALING:
                currentX = 1F;
                currentY = 1F;
                currentZ = 1F;
                break;
            case TRANSLATION:
            case ROTATION:
                currentX = 0F;
                currentY = 0F;
                currentZ = 0F;
        }

        this.modelId = modelId;
    }

    public Vector3f getCurrentVector() {
        return new Vector3f(getCurrentX(), getCurrentY(), getCurrentZ());
    }

    public boolean updateValues(int frame) {
        if (x != null) {
            if (frame > x.getValues().length - 1) return true;
            currentX = x.getValues()[frame];
        }
        if (y != null) {
            if (frame > y.getValues().length - 1) return true;
            currentY = y.getValues()[frame];
        }
        if (z != null) {
            if (frame > z.getValues().length - 1) return true;
            currentZ = z.getValues()[frame];
        }
        return false;
    }

    public float getCurrentX() {
        return currentX;
    }

    public float getCurrentY() {
        return currentY;
    }

    public float getCurrentZ() {
        return currentZ;
    }

    public FBXKeyFrame getZ() {
        return z;
    }

    public FBXKeyFrame getY() {
        return y;
    }

    public FBXKeyFrame getX() {
        return x;
    }

    public CurveType getType() {
        return type;
    }

    public long getModelId() {
        return modelId;
    }

    public enum CurveType {
        TRANSLATION,
        ROTATION,
        SCALING
    }
}
