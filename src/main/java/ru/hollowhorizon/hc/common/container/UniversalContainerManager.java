package ru.hollowhorizon.hc.common.container;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class UniversalContainerManager {
    private static final Map<String, Supplier<UniversalContainer>> CONTAINERS = new HashMap<>();

    public static void registerContainer(String id, Supplier<UniversalContainer> supplier) {
        CONTAINERS.put(id, supplier);
    }

    public static UniversalContainer getContainer(String id) {
        return CONTAINERS.get(id).get();
    }

    public static String getContainerId(UniversalContainer container) {
        for (Map.Entry<String, Supplier<UniversalContainer>> entry : CONTAINERS.entrySet()) {
            if (entry.getValue().get() == container) {
                return entry.getKey();
            }
        }
        return null;
    }
}
