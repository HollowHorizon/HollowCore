package ru.hollowhorizon.hc.common.registry;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;
import ru.hollowhorizon.hc.client.video.media.MediaEntry;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class VideoRegistry {
    public static final IForgeRegistry<MediaEntry> VIDEOS = RegistryManager.ACTIVE.getRegistry(MediaEntry.class);

    public static void init() {
        VideoRegistryKeys.init();
        makeRegistry(VideoRegistryKeys.VIDEOS, MediaEntry.class, "video").legacyName("videos").create();
    }

    private static <T extends IForgeRegistryEntry<T>> RegistryBuilder<T> makeRegistry(RegistryKey<? extends Registry<T>> key, Class<T> type, String _default) {
        return new RegistryBuilder<T>().setName(key.location()).setType(type).setMaxID(Integer.MAX_VALUE - 1).setDefaultKey(new ResourceLocation(_default));
    }

    public static class VideoRegistryKeys {
        public static final RegistryKey<Registry<MediaEntry>> VIDEOS = key("video");

        private static <T> RegistryKey<Registry<T>> key(String name) {
            return RegistryKey.createRegistryKey(new ResourceLocation(MODID, name));
        }

        private static void init() {
        }
    }
}
