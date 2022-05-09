package ru.hollowhorizon.hc.common.animations;

import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public class MobPosInfo {
    private final Vector3d mobPosition;
    private final Vector3f mobRotation;

    public MobPosInfo(Vector3d mobPosition, Vector3f mobRotation) {
        this.mobPosition = mobPosition;
        this.mobRotation = mobRotation;
    }

    public Vector3d getMobPosition() {
        return mobPosition;
    }

    public Vector3f getMobRotation() {
        return mobRotation;
    }

    public double getPosX() {
        return mobPosition.x;
    }

    public double getPosY() {
        return mobPosition.y;
    }

    public double getPosZ() {
        return mobPosition.z;
    }

    public float getRotX() {
        return mobRotation.x();
    }

    public float getRotY() {
        return mobRotation.y();
    }
}
