package ru.hollowhorizon.hc.common.story.cutscenes;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.vector.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class CutsceneBuilder {
    private final List<CutscenePart> parts = new ArrayList<>();

    public CutsceneBuilder() {
    }

    public CutsceneBuilder addPart(int ticks, Vector3d position) {
        parts.add(new CutscenePart(position, ticks));
        return this;
    }

    public void run(ServerPlayerEntity entity) {

    }

    public List<CutscenePart> getParts() {
        return parts;
    }
}
