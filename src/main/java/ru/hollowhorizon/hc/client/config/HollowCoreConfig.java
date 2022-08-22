package ru.hollowhorizon.hc.client.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.api.utils.HollowConfig;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HollowCoreConfig {
    public static final Map<String, List<Field>> FIELDS = new HashMap<>();

    @HollowConfig("general.main_hero_voice")
    public static boolean main_hero_voice = true;
    @HollowConfig("sound.dialogues_volume")
    public static float dialogues_volume = 1.0F;

    public static void save() {
        HollowCore.LOGGER.info("Saving config...");
        try {
            for (Map.Entry<String, List<Field>> entry : FIELDS.entrySet()) {

                File file = FMLPaths.GAMEDIR.get().resolve("config").resolve(entry.getKey() + ".toml").toFile();

                if (!file.exists()) file.createNewFile();

                CommentedFileConfig configuration = CommentedFileConfig.builder(file).autosave().build();

                for (Field field : entry.getValue()) {
                    HollowConfig config = field.getAnnotation(HollowConfig.class);

                    if (field.getType().equals(boolean.class)) {
                        boolean data = (boolean) field.get(null);
                        configuration.set(config.value(), data);
                    } else if (field.getType().equals(float.class)) {
                        float data = (float) field.get(null);
                        configuration.set(config.value(), data);
                    } else if (field.getType().equals(int.class)) {
                        int data = (int) field.get(null);
                        configuration.set(config.value(), data);
                    } else if (field.getType().equals(String.class)) {
                        String data = (String) field.get(null);
                        configuration.set(config.value(), data);
                    } else {
                        throw new IllegalArgumentException("Unsupported type: " + field.getType());
                    }

                    if(!config.description().isEmpty()) configuration.setComment(config.value(), config.description());
                }

                configuration.close();
            }
        } catch (IllegalAccessException | IOException e) {
            throw new RuntimeException(e);
        }
        HollowCore.LOGGER.info("Config saved.");
    }

    public static void load() {
        HollowCore.LOGGER.info("Loading config...");
        try {
            for (Map.Entry<String, List<Field>> entry : FIELDS.entrySet()) {
                File file = FMLPaths.GAMEDIR.get().resolve("config").resolve(entry.getKey() + ".toml").toFile();
                if (!file.exists()) {
                    save();
                }
                CommentedFileConfig configuration = CommentedFileConfig.builder(file).autosave().build();
                configuration.load();
                for (Field field : entry.getValue()) {
                    HollowConfig config = field.getAnnotation(HollowConfig.class);
                    if (field.getType().equals(boolean.class)) {
                        boolean data = configuration.get(config.value());
                        field.set(null, data);
                    } else if (field.getType().equals(float.class)) {
                        HollowCore.LOGGER.info("Loading float: " + configuration.get(config.value())+ " " + config.value());
                        double d = configuration.get(config.value());
                        float data = (float) d;
                        field.set(null, data);
                    } else if (field.getType().equals(int.class)) {
                        int data = configuration.get(config.value());
                        field.set(null, data);
                    } else if (field.getType().equals(String.class)) {
                        String data = configuration.get(config.value());
                        field.set(null, data);
                    } else {
                        throw new IllegalArgumentException("Unsupported type: " + field.getType());
                    }
                }
                configuration.close();
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        HollowCore.LOGGER.info("Config loaded.");
    }
}
