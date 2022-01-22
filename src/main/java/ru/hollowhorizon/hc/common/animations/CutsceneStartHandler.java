package ru.hollowhorizon.hc.common.animations;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import ru.hollowhorizon.hc.HollowCore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CutsceneStartHandler {
    private static final List<CutsceneHandler> allCutscenes = new ArrayList<>();

    public static void startUncompletedCutscene(ServerPlayerEntity player) {
        if (player.getPersistentData().contains("current_cutscene")) {
            CompoundNBT data = player.getPersistentData().getCompound("current_cutscene");

            String name = data.getString("cutscene_name");

            for (CutsceneHandler handler : allCutscenes) {
                if (handler.getName().equals(name)) {
                    int x = data.getInt("x");
                    int y = data.getInt("y");
                    int z = data.getInt("z");

                    player.teleportTo(x, y, z);

                    HollowCore.LOGGER.info("coordinates: "+x+" "+y+ " "+z);

                    handler.setStartPos(new BlockPos(x, y + 1, z));
                    handler.start(player);
                    return;
                }
            }
        }
    }

    public static void start(ServerPlayerEntity player, String cutsceneId) {
        for (CutsceneHandler handler : allCutscenes) {
            if (handler.getName().equals(cutsceneId)) {
                handler.start(player);
                return;
            }
        }
    }

    public static CutsceneHandler get(String cutsceneId) {
        for (CutsceneHandler handler : allCutscenes) {
            if (handler.getName().equals(cutsceneId)) {
                return handler;
            }
        }
        return null;
    }

    public static void register(Supplier<CutsceneHandler> handler) {
        allCutscenes.add(handler.get());
    }
}
