package ru.hollowhorizon.hc.common.story.events;


import ru.hollowhorizon.hc.common.story.HollowStoryHandler;

import java.util.ArrayList;
import java.util.List;

public class StoryRegistry {
    private static final List<Class<? extends HollowStoryHandler>> list = new ArrayList<>();

    public static void register(Class<? extends HollowStoryHandler> storyHandler) {
        list.add(storyHandler);
    }

    public static List<Class<? extends HollowStoryHandler>> getAllStories() {
        return list;
    }
}
