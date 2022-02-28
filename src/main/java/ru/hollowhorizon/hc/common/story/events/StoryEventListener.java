package ru.hollowhorizon.hc.common.story.events;

import net.minecraft.entity.player.ServerPlayerEntity;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.network.data.StoryInfoData;
import ru.hollowhorizon.hc.common.story.HollowStoryHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoryEventListener {
    public static final List<HollowStoryHandler> activeLore = new ArrayList<>();
    private static final Map<String, Class<? extends HollowStoryHandler>> handler = new HashMap<>();
    private static boolean isChanging = false;

    public static void registerLoreEvent(Class<? extends HollowStoryHandler> lore) {

        try {
            HollowStoryHandler loreHandler = lore.getConstructor().newInstance();
            String name = loreHandler.getStoryName();
            if (name.equals("null")) {
                throw new NullPointerException("Кто-то забыл указать имя сюжетного события!");
            } else if (handler.containsKey(name)) {
                throw new NullPointerException("Указанное имя сюжетного события повторяется, у каждого события оно должно быть одинаковым!");
            }
            handler.put(name, loreHandler.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAll() {
        return new ArrayList<>(handler.keySet());
    }

    public static void startLore(String name, ServerPlayerEntity playerEntity) {
        StoryInfoData.INSTANCE.createData(playerEntity, name);
        startLoreNoUpdate(name, playerEntity);
    }

    public static void startLoreNoUpdate(String name, ServerPlayerEntity playerEntity) {
        HollowCore.LOGGER.info("start: " + name);
        if (handler.containsKey(name)) {
            HollowCore.LOGGER.info("true: ");
            try {
                HollowStoryHandler lore = handler.get(name).getConstructor().newInstance();

                lore.start(playerEntity);
                isChanging = true;
                activeLore.add(lore);
                isChanging = false;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static boolean hasStory(String loreName, ServerPlayerEntity player) {
        for (HollowStoryHandler handler : activeLore) {
            if (handler.getStoryName().equals(loreName) && player.getUUID().equals(handler.player.getUUID())) {
                return true;
            }
        }
        return false;
    }

    public static void endStory(String lore, ServerPlayerEntity player) {
        HollowStoryHandler handler = null;
        for (HollowStoryHandler storyHandler : activeLore) {
            if (storyHandler.getStoryName().equals(lore) && player.getUUID().equals(storyHandler.player.getUUID())) {
                handler=storyHandler;
                break;
            }
        }
        if(handler!=null) handler.stop();
    }

    public static void stopStory(String lore, ServerPlayerEntity player) {
        activeLore.removeIf(handler -> handler.getStoryName().equals(lore) && player.getUUID().equals(handler.player.getUUID()));
    }

    public static void stopAll(ServerPlayerEntity player) {
        isChanging = true;
        activeLore.removeIf(handler -> handler.player.equals(player));
        isChanging = false;
    }

    public static void init() {

        for (Class<? extends HollowStoryHandler> clazz : StoryRegistry.getAllStories()) {
            HollowCore.LOGGER.info("StoryRegistry: " + clazz);
            registerLoreEvent(clazz);
        }
    }


}
