package ru.hollowhorizon.hc.client.render.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.utils.HollowJavaUtils;
import ru.hollowhorizon.hc.common.registry.ModEntities;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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
        for(Map.Entry<EntityType<?>, String> entry : mobData.entrySet()) {
            String mobName = HollowJavaUtils.unpackZipFromJar(new ResourceLocation(entry.getValue()));

            RenderingRegistry.registerEntityRenderingHandler(entry.getKey(), (manager) -> new HollowMobRenderer<>(manager, mobName));
        }
    }


    public static ResourceLocation getPlayerSkin() {
        Minecraft mc = Minecraft.getInstance();

        if (!(mc.getCameraEntity() instanceof AbstractClientPlayerEntity)) {
            return DefaultPlayerSkin.getDefaultSkin(UUID.randomUUID());
        }

        return ((AbstractClientPlayerEntity) mc.getCameraEntity()).getSkinTextureLocation();
    }
}
