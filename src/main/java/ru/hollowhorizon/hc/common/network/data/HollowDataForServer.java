package ru.hollowhorizon.hc.common.network.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.utils.HollowPaths;
import ru.hollowhorizon.hc.client.utils.SaveJsonHelper;

import java.io.File;
import java.util.ArrayList;

public interface HollowDataForServer {
    ArrayList<String> data = new ArrayList<>();

    String getFileName();

    default void init() {
        createFile();
        try {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            File loreFile = server.getWorldPath(HollowPaths.HollowCoreData).resolve(getFileName() + ".json").toFile();

            if (loreFile.exists()) {
                JsonArray array = SaveJsonHelper.readArray(loreFile);
                if (array != null) {
                    for(JsonElement element : array) {
                        data.add(element.getAsString());
                    }
                }
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    default void createFile() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        File loreFile = server.getWorldPath(HollowPaths.HollowCoreData).resolve(getFileName() + ".json").toFile();
        
        if (!loreFile.exists()) SaveJsonHelper.save(loreFile, new JsonArray());
    }

    default void addData(String string) {
        try {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            File loreFile = server.getWorldPath(HollowPaths.HollowCoreData).resolve(getFileName() + ".json").toFile();

            //Сохранение данных
            if (!loreFile.exists()) {
                if(!loreFile.getParentFile().mkdirs()) HollowCore.LOGGER.info("Can't create directories");

                JsonArray array = new JsonArray();

                array.add(new JsonPrimitive(string));

                SaveJsonHelper.save(loreFile, array);
            } else {
                JsonArray data = SaveJsonHelper.readArray(loreFile);

                if (data != null) {
                    for (JsonElement element : data) {
                        if (element.getAsString().equals(string)) return;
                    }
                    data.add(new JsonPrimitive(string));

                    SaveJsonHelper.save(loreFile, data);
                } else {
                    JsonArray array = new JsonArray();

                    array.add(new JsonPrimitive(string));

                    SaveJsonHelper.save(loreFile, array);
                }
            }

            //загрузка данных в обработчик
            data.add(string);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    default void removeData(String string) {
        try {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            File loreFile = server.getWorldPath(HollowPaths.HollowCoreData).resolve(getFileName() + ".json").toFile();

            if (loreFile.exists()) {
                JsonArray array = SaveJsonHelper.readArray(loreFile);
                JsonArray deletedData = new JsonArray();

                if (array != null) {
                    for (JsonElement element : array) {
                        if (!element.getAsString().equals(string)) deletedData.add(element);
                    }

                    SaveJsonHelper.save(loreFile, deletedData);
                }
            }

            data.remove(string);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    default boolean hasData(String string) {
        return data.contains(string);
    }

    default String[] getAll() {
        return data.toArray(new String[0]);
    }
}
