package ru.hollowhorizon.hc.common.registry

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraftforge.registries.DeferredRegister
import net.minecraftforge.registries.ForgeRegistries
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.objects.entities.TestEntity
import ru.hollowhorizon.hc.common.objects.entities.TestEntityV2

object ModEntities {
    @JvmField
    val ENTITIES: DeferredRegister<EntityType<*>> =
        DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, HollowCore.MODID)

    @JvmField
    val TEST_ENTITY = ENTITIES.register("test_entity") {
        EntityType.Builder.of(::TestEntity, MobCategory.CREATURE).sized(1f, 2f).build("test_entity")
    }
}
