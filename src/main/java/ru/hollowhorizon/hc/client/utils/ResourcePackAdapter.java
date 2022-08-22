package ru.hollowhorizon.hc.client.utils;

import net.minecraft.resources.IResourcePack;

import java.util.ArrayList;
import java.util.List;

public class ResourcePackAdapter {
    public static final List<IResourcePack> RESOURCE_PACKS = new ArrayList<>();

    public static void registerResourcePack(IResourcePack pack) {
        if(!RESOURCE_PACKS.contains(pack)) RESOURCE_PACKS.add(pack);
    }
}
