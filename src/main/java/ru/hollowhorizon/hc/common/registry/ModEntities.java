package ru.hollowhorizon.hc.common.registry;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import ru.hollowhorizon.hc.api.registy.HollowRegister;
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer;
import ru.hollowhorizon.hc.common.objects.entities.TestEntity;
import ru.hollowhorizon.hc.common.objects.entities.TestEntityV2;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class ModEntities {
    @HollowRegister(renderer = "ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer")
    public static final EntityType<TestEntity> TEST_ENTITY = createEntity(TestEntity::new, EntityClassification.CREATURE, 1, 2, "test_entity");

    @HollowRegister(renderer = "ru.hollowhorizon.hc.client.render.entity.TestEntityV2Renderer")
    public static final EntityType<TestEntityV2> TEST_ENTITY_V2 = createEntity(TestEntityV2::new, EntityClassification.CREATURE, 1, 2, "test_entity_v2");

    public static <T extends Entity> EntityType<T> createEntity(EntityType.IFactory<T> entity, EntityClassification classification, float width, float height, String regName) {
        return EntityType.Builder.of(entity, classification).sized(width, height).build(MODID + ":" + regName);
    }
}
