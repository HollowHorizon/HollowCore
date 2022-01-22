package ru.hollowhorizon.hc.client.utils;

import net.minecraft.resources.IResourcePack;

import java.util.ArrayList;
import java.util.List;

public class ResourcePackAdapter {
    public static final List<IResourcePack> BUILTIN_PACKS = new ArrayList<>();

    public static void registerResourcePack(IResourcePack pack) {
        if(!BUILTIN_PACKS.contains(pack)) BUILTIN_PACKS.add(pack);
    }
}
