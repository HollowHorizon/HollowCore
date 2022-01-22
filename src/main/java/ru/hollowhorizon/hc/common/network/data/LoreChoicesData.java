package ru.hollowhorizon.hc.common.network.data;

public class LoreChoicesData implements HollowDataForPlayer {
    public static final LoreChoicesData INSTANCE = new LoreChoicesData();

    @Override
    public String getFileName() {
        return "lore_progress";
    }
}
