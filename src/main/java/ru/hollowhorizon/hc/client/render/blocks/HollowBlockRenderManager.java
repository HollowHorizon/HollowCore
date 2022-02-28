package ru.hollowhorizon.hc.client.render.blocks;

import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.ClientRegistry;

import java.util.HashMap;
import java.util.Map;

public class HollowBlockRenderManager {
    private static final Map<TileEntityType<?>, String> blockData = new HashMap<>();

    public static void renderBlockModels() {
        registerHollowMobs();
    }

    public static void registerHollowMob(TileEntityType<?> entityType, String mobPath) {
        blockData.put(entityType, mobPath);
    }

    public static void registerHollowMobs() {
        for (Map.Entry<TileEntityType<?>, String> entry : blockData.entrySet()) {
            ClientRegistry.bindTileEntityRenderer(entry.getKey(), (manager -> new HollowBlockRenderer<>(manager, new ResourceLocation(entry.getValue()))));
        }
    }
}
