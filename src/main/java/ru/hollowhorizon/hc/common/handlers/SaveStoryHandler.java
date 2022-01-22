package ru.hollowhorizon.hc.common.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import ru.hollowhorizon.hc.client.utils.HollowPaths;
import ru.hollowhorizon.hc.client.utils.SaveJsonHelper;
import ru.hollowhorizon.hc.common.events.StoryAdvancementEvent;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class SaveStoryHandler {
    public static void saveStory(ServerPlayerEntity player, int slot) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        JsonArray loreProgress = SaveJsonHelper.readArray(server.getWorldPath(HollowPaths.HollowCoreData).resolve(player.getUUID().toString()).resolve("lore_progress.json").toFile());
        JsonArray loreList = SaveJsonHelper.readArray(server.getWorldPath(HollowPaths.HollowCoreData).resolve(player.getUUID().toString()).resolve("story_info.json").toFile());

        JsonObject position = new JsonObject();
        position.addProperty("x", player.getX());
        position.addProperty("y", player.getY());
        position.addProperty("z", player.getZ());
        position.addProperty("dimension", player.getLevel().dimension().location().toString());

        Path saveFolder = server.getWorldPath(HollowPaths.SaveData).resolve(player.getUUID().toString()).resolve(slot + "");

        if (loreProgress != null) SaveJsonHelper.save(saveFolder.resolve("lore_progress.json").toFile(), loreProgress);
        if (loreList != null) SaveJsonHelper.save(saveFolder.resolve("story_info.json").toFile(), loreList);
        SaveJsonHelper.save(saveFolder.resolve("position.json").toFile(), position);
        saveDate(player, slot);
    }

    public static void loadStory(ServerPlayerEntity player, int slot) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        JsonArray oldLoreProgress = SaveJsonHelper.readArray(server.getWorldPath(HollowPaths.HollowCoreData).resolve(player.getUUID().toString()).resolve("lore_progress.json").toFile());

        JsonArray loreProgress = SaveJsonHelper.readArray(server.getWorldPath(HollowPaths.SaveData).resolve(player.getUUID().toString()).resolve(slot + "").resolve("lore_progress.json").toFile());
        JsonArray loreList = SaveJsonHelper.readArray(server.getWorldPath(HollowPaths.SaveData).resolve(player.getUUID().toString()).resolve(slot + "").resolve("story_info.json").toFile());
        JsonObject position = SaveJsonHelper.readObject(server.getWorldPath(HollowPaths.SaveData).resolve(player.getUUID().toString()).resolve(slot + "").resolve("position.json").toFile());

        if (oldLoreProgress != null && loreProgress != null) {
            for (JsonElement element : oldLoreProgress) {
                if (!loreProgress.contains(element)) {
                    MinecraftForge.EVENT_BUS.post(new StoryAdvancementEvent(player, element.getAsString(), StoryAdvancementEvent.Type.TAKE));
                }
            }
        }

        if (loreProgress != null)
            SaveJsonHelper.save(server.getWorldPath(HollowPaths.HollowCoreData).resolve(player.getUUID().toString()).resolve("lore_progress.json").toFile(), loreProgress);
        if (loreList != null)
            SaveJsonHelper.save(server.getWorldPath(HollowPaths.HollowCoreData).resolve(player.getUUID().toString()).resolve("story_info.json").toFile(), loreList);
        if (position != null) {
            String[] dim = position.get("dimension").getAsString().split(":");
            ServerWorld dimension = server.getLevel(RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(dim[0], dim[1])));

            if (dimension != null) {
                player.teleportTo(dimension, position.get("x").getAsDouble(), position.get("y").getAsDouble(), position.get("z").getAsDouble(), 0, 0);
            }
        }
    }

    public static void saveDate(ServerPlayerEntity player, int slot) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        Path saveFolder = server.getWorldPath(HollowPaths.SaveData).resolve(player.getUUID().toString()).resolve(slot + "").resolve("date.json");

        JsonObject date = new JsonObject();
        date.addProperty("date", currentTime());

        SaveJsonHelper.save(saveFolder.toFile(), date);
    }

    public static ITextComponent getDate(ServerPlayerEntity player, int slot) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();

        Path saveFolder = server.getWorldPath(HollowPaths.SaveData).resolve(player.getUUID().toString()).resolve(slot + "").resolve("date.json");

        if (saveFolder.toFile().exists()) {
            JsonObject object = SaveJsonHelper.readObject(saveFolder.toFile());
            if (object != null && object.has("date")) {
                return new StringTextComponent(object.get("date").getAsString());
            }
        }
        return new TranslationTextComponent("hollowcore.gui.save_handler.empty");
    }

    public static String currentTime() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
        return sdf.format(cal.getTime());
    }
}
