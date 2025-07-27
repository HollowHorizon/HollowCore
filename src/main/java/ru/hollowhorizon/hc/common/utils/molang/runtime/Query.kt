package ru.hollowhorizon.hc.common.utils.molang.runtime

interface Query {
    val ground_speed: Float get() = 0f
    val is_moving: Boolean get() = false
    val is_sneaking: Boolean get() = false
    val is_sprinting: Boolean get() = false
    val is_jumping: Boolean get() = false
    val velocity_y: Float get() = 0f
    val velocity_x: Float get() = 0f
    val velocity_z: Float get() = 0f
    val is_flying: Boolean get() = false
    val fall_ticks: Float get() = 0f
    val is_swimming: Boolean get() = false
    val is_sitting: Boolean get() = false
    val is_sleeping: Boolean get() = false
    val is_hurt: Boolean get() = false
    val is_swinging: Boolean get() = false
    val is_alive: Boolean get() = true
    val is_on_ground: Boolean get() = true
    val head_rot: Float get() = 0f
    val anim_time: Float get() = 0f
}