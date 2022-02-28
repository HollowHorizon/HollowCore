package ru.hollowhorizon.hc.common.story.cutscenes;

import net.minecraft.util.math.vector.Vector3d;

public class CutscenePart {
    private final Vector3d position;
    private final int ticks;

    public CutscenePart(Vector3d position, int ticks) {
        this.position = position;
        this.ticks = ticks;
    }

    public int getTicks() {
        return ticks;
    }

    public Vector3d getPosition() {
        return position;
    }
}
