package ru.hollowhorizon.hc.client.particles.light

import dev.folomeev.kotgl.matrix.vectors.Vec3

fun interface LightProvider {
    fun query(pos: Vec3): Int
}