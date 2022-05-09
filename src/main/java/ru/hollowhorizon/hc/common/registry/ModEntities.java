package ru.hollowhorizon.hc.common.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.common.objects.entities.TestEntity;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class ModEntities {
    @HollowRegister(model = "hc:models/animstion.fbx")
    public static final EntityType<TestEntity> testEntity = createEntity(
            TestEntity::new,
            EntityClassification.CREATURE, 1F, 2F, "testentity"
    );

    private static <T extends Entity> EntityType<T> createEntity(EntityType.IFactory<T> entity, EntityClassification classification, float width, float height, String regName) {
        return EntityType.Builder.of(entity, classification).sized(width, height).build(MODID + ":" + regName);
    }
}
