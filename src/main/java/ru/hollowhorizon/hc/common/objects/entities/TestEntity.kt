package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.entity.EntityType
import net.minecraft.entity.MobEntity
import net.minecraft.entity.ai.goal.LookRandomlyGoal
import net.minecraft.potion.EffectInstance
import net.minecraft.world.World
import net.minecraftforge.common.capabilities.Capability
import ru.hollowhorizon.hc.client.gltf.GlTFModelManager
import ru.hollowhorizon.hc.client.gltf.IAnimatedEntity
import ru.hollowhorizon.hc.client.gltf.RenderedGltfModel
import ru.hollowhorizon.hc.client.gltf.animations.AnimationManager
import ru.hollowhorizon.hc.client.gltf.animations.AnimationTarget
import ru.hollowhorizon.hc.client.gltf.animations.CodeLayer
import ru.hollowhorizon.hc.common.capabilities.AnimatedEntityCapability
import ru.hollowhorizon.hc.common.capabilities.HollowCapabilityV2
import ru.hollowhorizon.hc.common.capabilities.ICapabilitySyncer
import ru.hollowhorizon.hc.common.capabilities.getCapability

class TestEntity(type: EntityType<TestEntity>, world: World) : MobEntity(type, world), IAnimatedEntity,
    ICapabilitySyncer {

    override fun registerGoals() {
        super.registerGoals()
        this.goalSelector.addGoal(1, LookRandomlyGoal(this))
    }

    override fun onEffectAdded(effect: EffectInstance) {
        super.onEffectAdded(effect)
    }

    override fun tick() {
        this.navigation.moveTo(8.0, 56.0, 6.0, 0.3)

        super.tick()
    }

    override fun onCapabilitySync(capability: Capability<*>) {
        if (capability == HollowCapabilityV2.get<AnimatedEntityCapability>() && level.isClientSide) {
            val animCapability = this.getCapability<AnimatedEntityCapability>()

            renderedGltfModel = GlTFModelManager.getOrCreate(this, animCapability)
            animationManager = AnimationManager(renderedGltfModel!!)
        }
    }


    override var renderedGltfModel: RenderedGltfModel? = null
    override var animationManager: AnimationManager? = null

}