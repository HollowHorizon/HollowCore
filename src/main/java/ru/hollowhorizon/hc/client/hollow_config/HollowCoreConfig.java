package ru.hollowhorizon.hc.client.hollow_config;

import com.google.gson.JsonObject;
import net.minecraftforge.fml.loading.FMLPaths;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.client.utils.SaveJsonHelper;

import java.io.File;

public class HollowCoreConfig {
    private static final File config = FMLPaths.CONFIGDIR.get().resolve("hollow_core.json").toFile();
    @HollowConfig
    public static HollowVariable<Boolean> main_hero_voice = new HollowVariable<>(true, "main_hero_voice");
    @HollowConfig
    public static HollowVariable<Boolean> is_editing_mode = new HollowVariable<>(false, "");
    @HollowConfig(min = -1.0F, max = 10.0F)
    public static HollowVariable<Float> dialogues_volume = new HollowVariable<>(1.0F, "");

    public static void initConfig() {

        if (!config.exists()) {
            SaveJsonHelper.save(config, new JsonObject());
        } else {

        }
    }

    public static void setInt(String target, int value) {
        JsonObject configObject = SaveJsonHelper.readObject(config);
        if (configObject != null) {
            configObject.addProperty(target, value);
            SaveJsonHelper.save(config, configObject);
        } else {
            throw new NullPointerException("Hollow Core Config corrupted, delete it and try again!");
        }
    }

    public static void setBool(String target, boolean value) {
        JsonObject configObject = SaveJsonHelper.readObject(config);
        if (configObject != null) {
            configObject.addProperty(target, value);
            SaveJsonHelper.save(config, configObject);
        } else {
            throw new NullPointerException("Hollow Core Config corrupted, delete it and try again!");
        }
    }

    public static void setFloat(String target, float value) {
        JsonObject configObject = SaveJsonHelper.readObject(config);
        if (configObject != null) {
            configObject.addProperty(target, value);
            SaveJsonHelper.save(config, configObject);
        } else {
            throw new NullPointerException("Hollow Core Config corrupted, delete it and try again!");
        }
    }

    public static boolean tryGetBool(String target, boolean defaultValue) {
        JsonObject obj = SaveJsonHelper.readObject(config);
        if(obj==null) return defaultValue;
        if (!obj.has(target)) {
            obj.addProperty(target, defaultValue);
            SaveJsonHelper.save(config, obj);
            return defaultValue;
        } else {
            return obj.get(target).getAsBoolean();
        }
    }

    public static int tryGetInt(String target, int defaultValue) {
        JsonObject obj = SaveJsonHelper.readObject(config);
        if(obj==null) return defaultValue;
        if (!obj.has(target)) {
            obj.addProperty(target, defaultValue);
            SaveJsonHelper.save(config, obj);
            return defaultValue;
        } else {
            return obj.get(target).getAsInt();
        }
    }

    public static float tryGetFloat(String target, float defaultValue) {
        JsonObject obj = SaveJsonHelper.readObject(config);
        if(obj==null) return defaultValue;
        if (!obj.has(target)) {
            obj.addProperty(target, defaultValue);
            SaveJsonHelper.save(config, obj);
            return defaultValue;
        } else {
            return obj.get(target).getAsFloat();
        }
    }
}
