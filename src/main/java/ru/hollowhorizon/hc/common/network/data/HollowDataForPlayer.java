package ru.hollowhorizon.hc.common.network.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import ru.hollowhorizon.hc.client.utils.HollowPaths;
import ru.hollowhorizon.hc.client.utils.SaveJsonHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public interface HollowDataForPlayer {
    String getFileName();

    default File getDataFile(ServerPlayerEntity player) {
        return getDataFile(player.getUUID().toString());
    }

    default File getDataFile(String player) {
        return ServerLifecycleHooks.getCurrentServer().getWorldPath(HollowPaths.HollowCoreData).resolve(player).resolve(getFileName() + ".json").toFile();
    }

    default void createFile(ServerPlayerEntity player) {
        if (!getDataFile(player).exists()) SaveJsonHelper.save(getDataFile(player), new JsonArray());
    }

    default void createData(String player, String name) {
        JsonArray array = SaveJsonHelper.readArray(getDataFile(player));
        if (!array.contains(new JsonPrimitive(name))) {
            array.add(new JsonPrimitive(name));
            SaveJsonHelper.save(getDataFile(player), array);
        }
    }

    default void createData(PlayerEntity player, String name) {
        createData(player.getUUID().toString(), name);
    }

    default void replaceData(PlayerEntity player, String oldValue, String newValue) {
        removeData(player, oldValue);
        createData(player, newValue);
    }

    default void replaceData(String player, String oldValue, String newValue) {
        removeData(player, oldValue);
        createData(player, newValue);
    }

    default void removeData(PlayerEntity player, String name) {
        removeData(player.getUUID().toString(), name);
    }

    default void removeData(String player, String name) {
        JsonArray array = SaveJsonHelper.readArray(getDataFile(player));
        if (array.contains(new JsonPrimitive(name))) {
            array.remove(new JsonPrimitive(name));
            SaveJsonHelper.save(getDataFile(player), array);
        }
    }

    default boolean hasData(PlayerEntity player, String name) {
        return SaveJsonHelper.readArray(getDataFile((ServerPlayerEntity) player)).contains(new JsonPrimitive(name));
    }

    default String[] getAll(PlayerEntity player) {
        return getAll(player.getUUID().toString());

    }

    default String[] getAll(String player) {
        ArrayList<String> list = new ArrayList<>();

        JsonArray array = SaveJsonHelper.readArray(getDataFile(player));
        array.forEach((element) -> list.add(element.getAsString()));

        return list.toArray(new String[0]);

    }


}
