package ru.hollowhorizon.hc.common.network.data;

import java.util.ArrayList;
import java.util.List;

public interface HollowDataHandler {
    List<String> structures = new ArrayList<>();
    List<String> names = new ArrayList<>();

    default void addData(String data) {
        structures.add(data);
        names.add(data.split(":")[0]);
    }

    default boolean hasData(String data) {
        return names.contains(data);
    }

    default void removeData(String data) {
        for(String text : structures) {
            String structureName = text.split(":")[0];
            if(structureName.equals(data)) {
                structures.remove(text);
                names.remove(structureName);
                return;
            }
        }
    }

    default String[] getAll() {
        return structures.toArray(new String[0]);
    }
}
