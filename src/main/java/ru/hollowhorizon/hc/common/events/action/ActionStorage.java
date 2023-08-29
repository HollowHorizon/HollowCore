package ru.hollowhorizon.hc.common.events.action;

import ru.hollowhorizon.hc.HollowCore;

import java.util.HashMap;
import java.util.Map;

public class ActionStorage {
    private static final Map<String, HollowAction> allActions = new HashMap<>();

    public static void registerAction(String name, HollowAction action) {
        allActions.put(name, action);
    }

    public static HollowAction getAction(String name) {
        return allActions.get(name);
    }

    public static String getName(HollowAction action) {
        for (String name : allActions.keySet()) {
            HollowAction action1 = allActions.get(name);

            HollowCore.LOGGER.info(action1.equals(action));
            if (action1.equals(action)) return name;
        }
        return null;
    }
}
