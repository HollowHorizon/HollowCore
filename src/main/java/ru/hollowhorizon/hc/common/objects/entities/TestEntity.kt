package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.entity.EntityType
import net.minecraft.entity.MobEntity
import net.minecraft.network.datasync.EntityDataManager
import net.minecraft.potion.EffectInstance
import net.minecraft.util.ResourceLocation
import net.minecraft.world.World
import ru.hollowhorizon.hc.client.gltf.IAnimated
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimationManager
import ru.hollowhorizon.hc.common.objects.entities.data.ANIMATION_MANAGER

val ANIMATION_MANAGER_DATA = EntityDataManager.defineId(TestEntity::class.java, ANIMATION_MANAGER)

class TestEntity(type: EntityType<TestEntity>, world: World) : MobEntity(type, world), IAnimated {
    private var wasInWater = false
    override fun defineSynchedData() {
        super.defineSynchedData()
        entityData.define(ANIMATION_MANAGER_DATA, GLTFAnimationManager())
    }

    override fun onEffectAdded(effect: EffectInstance) {
        if (!this.level.isClientSide) this.getManager().addAnimation("hovering_idle", loop = false)
        super.onEffectAdded(effect)
    }

    override fun tick() {
        this.getManager().addAnimation("Thomas_rig|Thomas_rig|anim", loop = true)


        super.tick()
    }

    override fun getModel(): ResourceLocation {
        return ResourceLocation("hc", "models/entity/untitled.gltf")
    }

    override fun getManager(): GLTFAnimationManager {
        return entityData[ANIMATION_MANAGER_DATA]
    }

    override fun setManager(manager: GLTFAnimationManager) {
        entityData.set(ANIMATION_MANAGER_DATA, manager)
    }


}