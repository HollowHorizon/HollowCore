package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.entity.EntityType
import net.minecraft.entity.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResultType
import net.minecraft.util.Hand
import net.minecraft.util.math.vector.Vector3d
import net.minecraft.world.World
import ru.hollowhorizon.hc.client.models.core.model.BTAnimatedModel
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.objects.entities.animation.AnimationComponent
import ru.hollowhorizon.hc.common.objects.entities.animation.AnimationState
import ru.hollowhorizon.hc.common.objects.entities.animation.layers.FullBodyPoseLayer
import ru.hollowhorizon.hc.common.objects.entities.animation.layers.HeadTrackingLayer
import ru.hollowhorizon.hc.common.objects.entities.animation.layers.LocomotionLayer
import ru.hollowhorizon.hc.common.objects.entities.animation.layers.SubTreePoseLayer
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.PopStateMessage
import ru.hollowhorizon.hc.common.objects.entities.animation.messages.PushStateMessage
import ru.hollowhorizon.hc.common.registry.ModModels

class TestEntityV2(type: EntityType<TestEntityV2>, world: World) : MobEntity(type, world), IBTAnimatedEntity<TestEntityV2> {
    override val skeleton = ModModels.BIPED.skeleton.orElseThrow { IllegalStateException("Skeleton is not present") }
    override val animationComponent = AnimationComponent(this)

    init {
        setupAnimationComponent()
    }

    companion object {
        private val IDLE_ANIM = "hc:biped_idle".rl
        private val RUN_ANIM = "hc:biped_run".rl
        private val ZOMBIE_ARMS_ANIM = "hc:biped_zombie_arms".rl
        private val BACKFLIP_ANIM = "hc:biped_backflip".rl
    }

    override fun interactAt(player: PlayerEntity, vec: Vector3d, hand: Hand): ActionResultType {
        if (!level.isClientSide && hand == player.usedItemHand) {
            animationComponent.updateState(PushStateMessage("flip"))
            isNoAi = true
        }
        return super.interactAt(player, vec, hand)
    }

    private fun setupAnimationComponent() {
        val headTrackingLayer = HeadTrackingLayer("head", this, "bn_head")
        val locomotionLayer = LocomotionLayer("locomotion", IDLE_ANIM, RUN_ANIM, this, true)
        val armsLayer = SubTreePoseLayer("arms", ZOMBIE_ARMS_ANIM, this, true, "bn_chest")
        val defaultState = AnimationState("default", this).apply {
            addLayer(locomotionLayer)
            addLayer(armsLayer)
            addLayer(headTrackingLayer)
        }

        val flipLayer = FullBodyPoseLayer("flip", BACKFLIP_ANIM, this, false).apply {
            setEndCallback {
                if (!level.isClientSide) {
                    animationComponent.updateState(PopStateMessage())
                    isNoAi = false
                }
            }
        }
        val flipState = AnimationState("flip", this).apply {
            addLayer(flipLayer)
        }

        animationComponent.apply {
            addAnimationState(defaultState)
            pushState("default")
            addAnimationState(flipState)
        }
    }

    override fun tick() {
        super.tick()
        animationComponent.update()
    }
}