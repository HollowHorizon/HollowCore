package ru.hollowhorizon.hc.client.particles.collision

import dev.folomeev.kotgl.matrix.vectors.Vec3

fun interface CollisionProvider {
    fun query(pos: Vec3, size: Float, offset: Vec3): Pair<Vec3, Vec3>?

    object None : CollisionProvider {
        override fun query(pos: Vec3, size: Float, offset: Vec3): Pair<Vec3, Vec3>? = null
    }
}