package ru.hollowhorizon.hc.common.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class ModEntities {

    public static <T extends Entity> EntityType<T> createEntity(EntityType.IFactory<T> entity, EntityClassification classification, float width, float height, String regName) {
        return EntityType.Builder.of(entity, classification).sized(width, height).build(MODID + ":" + regName);
    }
}
