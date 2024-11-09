package ru.hollowhorizon.hc.client.particles

import dev.folomeev.kotgl.matrix.vectors.Vec3
import dev.folomeev.kotgl.matrix.vectors.vecZero
import ru.hollowhorizon.hc.client.utils.math.Quaternion

interface Transform {
    val parent: Transform?
    val isValid: Boolean
    val position: Vec3
    val rotation: Quaternion
    val velocity: Vec3

    object Zero : Transform {
        override val parent: Transform? get() = null
        override val isValid: Boolean get() = true
        override val position: Vec3 get() = vecZero()
        override val rotation: Quaternion get() = Quaternion.Identity
        override val velocity: Vec3 get() = vecZero()
    }

    companion object {
        fun create(pos: Vec3 = vecZero(), rotation: Quaternion = Quaternion.Identity): Transform = object: Transform {
            override val parent: Transform? get() = null
            override val isValid: Boolean get() = true
            override val position: Vec3 get() = pos
            override val rotation: Quaternion get() = rotation
            override val velocity: Vec3 get() = vecZero()
        }
    }
}