package ru.hollowhorizon.hc.client.particles.light

import org.joml.Vector3f

fun interface LightProvider {
    fun query(pos: Vector3f): Int
}