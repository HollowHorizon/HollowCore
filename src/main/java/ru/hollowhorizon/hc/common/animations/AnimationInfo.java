package ru.hollowhorizon.hc.common.animations;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Информация о анимации (логично, не так ли)
 */
public class AnimationInfo {
    private final LivingEntity player;
    private final List<MobPosInfo> mobPos = new ArrayList<>();
    private Vector3d startPos;
    private int counter = 0;
    private boolean isPause = false;
    private boolean isEndless = false;

    public AnimationInfo(LivingEntity player) {
        this.player = player;
    }

    public LivingEntity getPlayer() {
        return player;
    }

    public List<MobPosInfo> getMobPos() {
        return mobPos;
    }

    public void addPosInfo(Vector3d pos, Vector3f rot) {
        mobPos.add(new MobPosInfo(pos, rot));
    }

    public void addPosInfo(double posX, double posY, double posZ, float rotX, float rotY, float rotZ) {
        addPosInfo(new Vector3d(posX, posY, posZ), new Vector3f(rotX, rotY, rotZ));
    }

    public boolean isPause() {
        return isPause;
    }

    public void setPause(boolean pause) {
        isPause = pause;
    }

    public void setStartPos(double x, double y, double z) {
        this.startPos = new Vector3d(x, y, z);
    }

    public Vector3d getStartPos() {
        return startPos;
    }

    public boolean isEndless() {
        return isEndless;
    }

    public void setEndless(boolean endless) {
        isEndless = endless;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public void tickCounter() {
        this.counter++;
    }
}
