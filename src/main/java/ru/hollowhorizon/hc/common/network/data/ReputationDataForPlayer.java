package ru.hollowhorizon.hc.common.network.data;

import net.minecraft.entity.player.ServerPlayerEntity;

public class ReputationDataForPlayer implements HollowDataForPlayer {
    public static final ReputationDataForPlayer INSTANCE = new ReputationDataForPlayer();

    public void addReputation(ServerPlayerEntity player, String object, int reputation) {
        String[] objects = getAll(player);
        boolean contains = false;
        for (String data : objects) {
            String[] target = data.split(":", 2);

            String currentData = target[0];
            int currentReputation = Integer.parseInt(target[1]);

            if (currentData.equals(object)) {
                replaceData(player, data, currentData + ":" + (currentReputation + reputation));
                contains = true;
            }
        }
        if (!contains) createData(player, object + ":" + reputation);
    }

    public void removeReputation(ServerPlayerEntity player, String object, int reputation) {
        String[] objects = getAll(player);
        boolean contains = false;
        for (String data : objects) {
            String[] target = data.split(":", 2);

            String currentData = target[0];
            int currentReputation = Integer.parseInt(target[1]);

            if (currentData.equals(object)) {
                replaceData(player, data, currentData + ":" + (currentReputation - reputation));
                contains = true;
            }
        }
        if (!contains) createData(player, object + ":" + (-reputation));
    }

    public void setReputation(ServerPlayerEntity player, String object, int reputation) {
        String[] objects = getAll(player);
        boolean contains = false;
        for (String data : objects) {
            String[] target = data.split(":", 2);

            String currentData = target[0];

            if (currentData.equals(object)) {
                replaceData(player, data, currentData + ":" + (reputation));
                contains = true;
            }
        }
        if (!contains) createData(player, object + ":" + reputation);
    }

    public void clearReputation(ServerPlayerEntity player, String object) {
        String[] objects = getAll(player);
        for (String data : objects) {
            String[] target = data.split(":", 2);

            String currentData = target[0];
            int currentReputation = Integer.parseInt(target[1]);

            if (currentData.equals(object)) {
                removeData(player, object + ":" + currentReputation);
            }
        }
    }

    public int getReputation(ServerPlayerEntity player, String object) {
        String[] objects = getAll(player);
        for (String data : objects) {
            String[] target = data.split(":", 2);

            String currentData = target[0];
            int currentReputation = Integer.parseInt(target[1]);

            if (currentData.equals(object)) {
                return currentReputation;
            }
        }
        return 0;
    }

    @Override
    public String getFileName() {
        return "reputation";
    }
}
