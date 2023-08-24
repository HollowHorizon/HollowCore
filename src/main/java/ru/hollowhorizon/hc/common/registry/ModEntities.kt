package ru.hollowhorizon.hc.common.registry

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.MobCategory
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer
import ru.hollowhorizon.hc.common.objects.entities.TestEntity

object ModEntities : HollowRegistry() {

    val TEST_ENTITY by register(
        ObjectConfig(
            name = "test_entity",
            entityRenderer = "ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer",
            attributeSupplier = { Mob.createMobAttributes().build() }
        )
    ) {
        EntityType.Builder.of(::TestEntity, MobCategory.CREATURE).sized(1f, 2f).build("test_entity")
    }
}
