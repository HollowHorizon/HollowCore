package ru.hollowhorizon.hc.common.story.events;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.handlers.ClientTickHandler;
import ru.hollowhorizon.hc.common.story.HollowStoryHandler;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoryEventListener {
    public static final List<HollowStoryHandler> activeLore = new ArrayList<>();
    private static final Map<String, Class<? extends HollowStoryHandler>> handler = new HashMap<>();

    public static void registerLoreEvent(Class<? extends HollowStoryHandler> lore) {
        try {
            HollowStoryHandler loreHandler = lore.getConstructor().newInstance();
            String name = loreHandler.getStoryName();
            if (name.equals("null")) {
                throw new NullPointerException("Кто-то забыл указать имя сюжетного события!");
            } else if (handler.containsKey(name)) {
                throw new NullPointerException("Указанное имя события повторяется, у каждого события оно должно быть одинаковым! Ещё раз так ошибёшься и я сломаю тебе винду");
            }
            handler.put(name, loreHandler.getClass());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<String> getAll() {
        return new ArrayList<>(handler.keySet());
    }

    public static void startLore(String name, PlayerEntity playerEntity) {
        HollowCore.LOGGER.info("start: " + name);
        if (handler.containsKey(name)) {
            HollowCore.LOGGER.info("true: ");
            try {
                HollowStoryHandler lore = handler.get(name).getConstructor().newInstance();

                lore.start(playerEntity);
                activeLore.add(lore);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void updateStory(String storyName, PlayerEntity player, CompoundNBT nbt) {
        for (HollowStoryHandler handler : activeLore) {
            if (handler.getStoryName().equals(storyName) && player.getUUID().equals(handler.player.getUUID())) {
                handler.loadNBT(nbt);
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

    public static void endStory(String lore, PlayerEntity player) {
        HollowStoryHandler handler = null;
        for (HollowStoryHandler storyHandler : activeLore) {
            if (storyHandler.getStoryName().equals(lore) && player.getUUID().equals(storyHandler.player.getUUID())) {
                handler = storyHandler;
                break;
            }
        }
        if (handler != null) handler.stop();
    }

    public static void stopStory(String lore, PlayerEntity player) {
        activeLore.removeIf(handler -> handler.getStoryName().equals(lore) && player.getUUID().equals(handler.player.getUUID()));
    }

    public static void stopAll(ServerPlayerEntity player) {
        activeLore.removeIf(handler -> handler.player.equals(player));
    }

    public static void init() {

        for (Class<? extends HollowStoryHandler> clazz : StoryRegistry.getAllStories()) {
            HollowCore.LOGGER.info("StoryRegistry: " + clazz);
            registerLoreEvent(clazz);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void updateStory(String storyName, CompoundNBT nbt) {
        updateStory(storyName, Minecraft.getInstance().player, nbt);
    }
}
