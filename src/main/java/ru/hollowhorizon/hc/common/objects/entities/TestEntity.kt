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
import ru.hollowhorizon.hc.common.capabilities.*

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

        this.level.getNearestPlayer(this, -1.0)?.let { this.navigation.moveTo(it, 0.3) }

        super.tick()
    }

    override fun onCapabilitySync(capability: Capability<*>) {
        if (capability == HollowCapabilityV2.get<AnimatedEntityCapability>() && level.isClientSide) {
            val animCapability = this.getCapability<AnimatedEntityCapability>()

            model = GlTFModelManager.getOrCreate(animCapability.model).apply {
                manager.addAnimation("animation.npcsteve.happy")
                manager.addAnimation("animation.npcsteve.blinking")
            }



            animCapability.syncEntity(this)
        }
    }


    override var model: RenderedGltfModel? = null

}