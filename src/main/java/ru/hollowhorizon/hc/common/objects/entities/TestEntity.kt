package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.level.Level
import ru.hollowhorizon.hc.client.gltf.IAnimated
import ru.hollowhorizon.hc.client.gltf.animations.manager.IModelManager
import ru.hollowhorizon.hc.client.utils.rl

class TestEntity(type: EntityType<TestEntity>, world: Level) : PathfinderMob(type, world), IAnimated {

    override fun registerGoals() {
        super.registerGoals()
        this.goalSelector.addGoal(0, RandomLookAroundGoal(this))
        this.goalSelector.addGoal(1, RandomStrollGoal(this, 1.0, 10))
    }

    override fun customServerAiStep() {
        super.customServerAiStep()
    }

    override val model = "hc:models/entity/player_model.gltf".rl

    //если сделать напрямую тут инициализацию, то Kotlin каждый раз будет новый менеджер анимаций создавать, что нам не нужно
    override val manager by lazy { IModelManager.create(this) }

}