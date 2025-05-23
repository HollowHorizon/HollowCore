package ru.hollowhorizon.hc.client.models.internal.controller

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.client.models.internal.manager.AnimatedEntityCapability
import ru.hollowhorizon.hc.client.models.internal.manager.GltfManager
import ru.hollowhorizon.hc.client.models.internal.manager.IAnimated
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer.Companion.MOVEMENT_FACTOR
import ru.hollowhorizon.hc.common.capabilities.CapabilityInstance
import ru.hollowhorizon.hc.common.capabilities.HollowCapability
import ru.hollowhorizon.hc.common.utils.get
import ru.hollowhorizon.hc.common.utils.molang.calculateSpeedViaDeltaMovement
import ru.hollowhorizon.hc.common.utils.rl
import kotlin.math.abs

@HollowCapability(IAnimated::class, Player::class)
class AnimationController : CapabilityInstance() {
    val entity by lazy {
        (provider as LivingEntity)
    }

    val controller by lazy {
        animationController {
            val animations =
                GltfManager.getOrCreate(entity[AnimatedEntityCapability::class].model.rl).animationPlayer.nameToAnimationMap
            layer("Basic") {
                stateMachine(animations) {
                    val idle = state("Idle") {
                        clip("Idle", wrap = WrapMode.Loop)
                    }
                    val move = state("Move") {
                        blendTree {
                            factor("q.ground_speed", 0.75f)
                            clip("Walking", 2.35f, speed = "min(q.ground_speed / 2, 4f)")
                            clip("Running", 3f, speed = "min(q.ground_speed / 2, 3f)")
                        }
                    }
                    val sneaking = state("Sneaking") {
                        clip(
                            "Sneaking",
                            wrap = WrapMode.Loop,
                            speed = "min(q.ground_speed / 2, 3f)"
                        )
                    }
                    val crouchWalking = state("CrouchWalking") {
                        clip(
                            "Crouth_Walking",
                            wrap = WrapMode.Loop,
                            speed = "min(q.ground_speed, 3f) * 2"
                        )
                    }

                    transition(idle, move) {
                        condition("q.is_moving")
                        duration(0.25f)
                    }
                    transition(move, idle) {
                        condition("!q.is_moving")
                        duration(0.25f)
                    }
                    transition(move, crouchWalking) {
                        condition("q.is_sneaking")
                        duration(0.25f)
                    }
                    transition(idle, sneaking) {
                        condition("q.is_sneaking")
                        duration(0.25f)
                    }
                    transition(sneaking, idle) {
                        condition("!q.is_sneaking")
                        duration(0.25f)
                    }
                    transition(sneaking, crouchWalking) {
                        condition("q.is_moving")
                        duration(0.25f)
                    }
                    transition(crouchWalking, sneaking) {
                        condition("!q.is_moving && q.is_sneaking")
                        duration(0.25f)
                    }
                    transition(crouchWalking, idle) {
                        condition("!q.is_moving && !q.is_sneaking")
                        duration(0.25f)
                    }
                    transition(crouchWalking, move) {
                        condition("!q.is_sneaking")
                        duration(0.25f)
                    }
                }
            }
            layer("Head") {
                stateMachine(animations) {
                    state("Idle") {
                        procedural {
                            onEvaluate {
                                listOf(
                                    "Head"
                                ).forEach { bone ->
                                    it.setBoneRotation(
                                        bone,
                                        "q.head_rot"
                                    )
                                }
                            }
                        }
                    }
                }
            }
            layer("Eyes", priority = 10) {
                stateMachine(animations) {
                    state("Eyes") {
                        clip("FaceLoop", wrap = WrapMode.Loop)
                    }
                }
            }
        }
    }
}

private val LivingEntity.animationSpeed: Float
    get() = calculateSpeedViaDeltaMovement(this)

private fun LivingEntity.isMoving() = abs(animationSpeed) >= MOVEMENT_FACTOR