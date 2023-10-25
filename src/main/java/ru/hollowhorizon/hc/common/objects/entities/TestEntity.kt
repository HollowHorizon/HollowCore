package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.level.Level
import ru.hollowhorizon.hc.client.models.gltf.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.gltf.manager.IAnimated
import ru.hollowhorizon.hc.client.utils.get


class TestEntity(type: EntityType<TestEntity>, world: Level) : PathfinderMob(type, world), IAnimated {

    init {
        this[AnimatedEntityCapability::class].model = "hc:models/entity/player_model.gltf"
    }

    override fun registerGoals() {
        super.registerGoals()
        this.goalSelector.addGoal(0, RandomLookAroundGoal(this))
        this.goalSelector.addGoal(1, RandomStrollGoal(this, 1.0, 10))
    }

    //если сделать напрямую тут инициализацию, то Kotlin каждый раз будет новый менеджер анимаций создавать, что нам не нужно
    //override val manager by lazy { IModelManager.create(this) }

}