package ru.hollowhorizon.hc.common.network.data;

import net.minecraft.entity.player.PlayerEntity;
import ru.hollowhorizon.hc.common.events.action.ActionPackage;

import java.util.ArrayList;
import java.util.List;

public class ActionsData implements HollowDataForPlayer {
    public static final ActionsData INSTANCE = new ActionsData();
    @Override
    public String getFileName() {
        return "actions";
    }

    public void addActionData(String playerId, String actionName, int ticks) {
        String[] objects = getAll(playerId);
        boolean contains = false;
        for (String data : objects) {
            String[] target = data.split(":", 2);
            String currentData = target[0];
            if (currentData.equals(actionName)) {
                replaceData(playerId, data, currentData + ":" + ticks);
                contains = true;
            }
        }
        if (!contains) createData(playerId, actionName + ":" + ticks);
    }

    public void removeActionData(PlayerEntity player, String name) {
        String[] objects = getAll(player);
        for (String data : objects) {
            String[] target = data.split(":", 2);
            String currentData = target[0];
            if (currentData.equals(name)) {
                removeData(player, data);
            }
        }
    }

    public List<ActionPackage> getAllActions(PlayerEntity player) {
        List<ActionPackage> dataList = new ArrayList<>();
        String[] objects = getAll(player);
        for (String data : objects) {
            String[] target = data.split(":", 2);
            String currentData = target[0];
            int ticks = Integer.parseInt(target[1]);
            dataList.add(new ActionPackage(currentData, ticks));
        }
        return dataList;
    }
}
