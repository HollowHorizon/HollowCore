package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.entity.EntityType
import net.minecraft.entity.MobEntity
import net.minecraft.network.datasync.DataParameter
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.potion.EffectInstance
import net.minecraft.world.World
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimationManager
import ru.hollowhorizon.hc.client.gltf.IAnimatedEntity
import ru.hollowhorizon.hc.common.objects.entities.data.ANIMATION_MANAGER

val ANIMATION_MANAGER_DATA = EntityDataManager.defineId(TestEntity::class.java, ANIMATION_MANAGER)

class TestEntity(type: EntityType<TestEntity>, world: World) : MobEntity(type, world), IAnimatedEntity {
    private var wasInWater = false
    override val ANIMATED_MODEL_DATA: DataParameter<GLTFAnimationManager> = ANIMATION_MANAGER_DATA

    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(ANIMATION_MANAGER_DATA, GLTFAnimationManager())
    }

    override fun onEffectAdded(effect: EffectInstance) {
        if (!this.level.isClientSide) this.addAnimation("Thomas_rigAction.001", loop = false)
        super.onEffectAdded(effect)
    }

    override fun tick() {

        if (this.isInWater) {
            this.addAnimation("Thomas_rigAction.001")
            wasInWater = true
        } else {
            if (wasInWater) {
                this.stopAnimation("Thomas_rigAction.001")
                wasInWater = false
            }
        }


        super.tick()
    }


}