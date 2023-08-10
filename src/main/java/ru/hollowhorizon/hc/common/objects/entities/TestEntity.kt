package ru.hollowhorizon.hc.common.objects.entities

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.Capability
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.gltf.GlTFModelManager
import ru.hollowhorizon.hc.client.gltf.IAnimatedEntity
import ru.hollowhorizon.hc.client.gltf.RenderedGltfModel
import ru.hollowhorizon.hc.client.gltf.animations.AnimationType
import ru.hollowhorizon.hc.common.capabilities.*

class TestEntity(type: EntityType<TestEntity>, world: Level) : Mob(type, world), IAnimatedEntity,
    ICapabilitySyncer {

    override fun registerGoals() {
        super.registerGoals()
        this.goalSelector.addGoal(1, RandomLookAroundGoal(this))
    }

    override fun onEffectAdded(pInstance: MobEffectInstance, pEntity: Entity?) {
        super.onEffectAdded(pInstance, pEntity)
    }

    override fun tick() {

        this.level.getNearestPlayer(this, -1.0)?.let { this.navigation.moveTo(it, 0.3) }

        super.tick()
    }

    override fun onCapabilitySync(capability: Capability<*>) {
        if (capability == HollowCapabilityV2.get(AnimatedEntityCapability::class.java) && level.isClientSide) {
            val animCapability = this.getCapability(AnimatedEntityCapability::class)

            RenderSystem.recordRenderCall {
                HollowCore.LOGGER.info("Loading model: {}", animCapability.model)
                model = GlTFModelManager.getOrCreate(animCapability.model).apply {
                    animCapability.transform = Transform(
                        rY = 180f
                    )
                    AnimationType.load(gltfModel, animCapability)
                    //manager.addAnimation("animation.npcsteve.happy")
                    //manager.addAnimation("animation.npcsteve.blinking")
                }
            }


            animCapability.syncEntity(this)
        }
    }


    override var model: RenderedGltfModel? = null

}