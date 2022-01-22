package ru.hollowhorizon.hc.common.network.data;

public class StoryInfoData implements HollowDataForPlayer{
    public static final StoryInfoData INSTANCE = new StoryInfoData();

    @Override
    public String getFileName() {
        return "story_info";
    }
}
