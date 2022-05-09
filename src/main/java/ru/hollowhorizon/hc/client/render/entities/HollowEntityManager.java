package ru.hollowhorizon.hc.client.render.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.common.registry.ModEntities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HollowEntityManager {
    private static final Map<EntityType<?>, String> mobData = new HashMap<>();

    public static void renderEntitiesModels() {
        registerHollowMobs();
    }

    public static void registerHollowMob(EntityType<?> entityType, String mobPath) {
        mobData.put(entityType, mobPath);
    }

    public static void registerHollowMobs() {
        HollowCore.LOGGER.info("models init");
        RenderingRegistry.registerEntityRenderingHandler(ModEntities.testEntity, AnimatedModelRenderer::new);
//        for(Map.Entry<EntityType<?>, String> entry : mobData.entrySet()) {
//            RenderingRegistry.registerEntityRenderingHandler(entry.getKey(), AnimatedModelRenderer::new);
//            HollowCore.LOGGER.info("model: "+entry.getKey().toString());
//        }
    }


    public static ResourceLocation getPlayerSkin() {
        Minecraft mc = Minecraft.getInstance();

        if (!(mc.getCameraEntity() instanceof AbstractClientPlayerEntity)) {
            return DefaultPlayerSkin.getDefaultSkin(UUID.randomUUID());
        }

        return ((AbstractClientPlayerEntity) mc.getCameraEntity()).getSkinTextureLocation();
    }
}
