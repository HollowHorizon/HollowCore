package ru.hollowhorizon.hc.common.objects.entities

import com.mojang.blaze3d.systems.RenderSystem
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.level.Level
import net.minecraftforge.common.capabilities.Capability
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.common.capabilities.*

class TestEntity(type: EntityType<TestEntity>, world: Level) : PathfinderMob(type, world), //IAnimatedEntity,
    ICapabilitySyncer {

    override fun registerGoals() {
        super.registerGoals()
        this.goalSelector.addGoal(1, RandomLookAroundGoal(this))
        this.goalSelector.addGoal(1, RandomStrollGoal(this, 1.0, 10))
    }




    override fun onCapabilitySync(capability: Capability<*>) {
        if (capability == HollowCapabilityV2.get(AnimatedEntityCapability::class.java) && level.isClientSide) {
            val animCapability = this.getCapability(AnimatedEntityCapability::class)

            RenderSystem.recordRenderCall {
                HollowCore.LOGGER.info("Loading model: {}", animCapability.model)
//                model = GlTFModelManager.getOrCreate(animCapability.model).apply {
//                    animCapability.transform = Transform(
//                        rY = 180f
//                    )
//                    AnimationType.load(gltfModel, animCapability)
//                    //manager.addAnimation("animation.npcsteve.happy")
//                    //manager.addAnimation("animation.npcsteve.blinking")
//                }
            }


            animCapability.syncEntity(this)
        }
    }


    //override var model: RenderedGltfModel? = null

}