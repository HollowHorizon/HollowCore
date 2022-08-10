package ru.hollowhorizon.hc.client.hollowconfig;

import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.gson.JsonObject;
import net.minecraftforge.fml.loading.FMLPaths;
import ru.hollowhorizon.hc.api.utils.HollowConfig;
import ru.hollowhorizon.hc.client.utils.SaveJsonHelper;

import java.io.File;

public class HollowCoreConfig {
    private static final File config = FMLPaths.CONFIGDIR.get().resolve("hollow_core.json").toFile();
    @HollowConfig("main_hero_voice")
    public static boolean main_hero_voice = true;
    @HollowConfig(value="dialogues_volume")
    public static float dialogues_volume = 1.0F;

    public static void initConfig() {
        TomlWriter writer = new TomlWriter();

        if (!config.exists()) {
            SaveJsonHelper.save(config, new JsonObject());
        } else {
            config.mkdirs();
        }
    }
}
