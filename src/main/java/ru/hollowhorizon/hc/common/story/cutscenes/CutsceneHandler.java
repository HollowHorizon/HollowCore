package ru.hollowhorizon.hc.common.story.cutscenes;

import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CutsceneHandler {
    private static final Map<ServerPlayerEntity, List<CutscenePart>> runningCutscenes = new HashMap<>();

    public static void addCutscene(ServerPlayerEntity player, CutsceneBuilder builder) {
        runningCutscenes.put(player, cloneList(builder.getParts()));
    }

    private static <T> List<T> cloneList(List<T> list) {
        List<T> clone = new ArrayList<>(list.size());
        clone.addAll(list);
        return clone;
    }
}
