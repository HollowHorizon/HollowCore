package ru.hollowhorizon.hc.client.particles

import org.joml.Vector3f
import ru.hollowhorizon.hc.client.utils.math.Quaternion

interface Transform {
    val parent: Transform?
    val isValid: Boolean
    val position: Vector3f
    val rotation: Quaternion
    val velocity: Vector3f

    object Zero : Transform {
        override val parent: Transform? get() = null
        override val isValid: Boolean get() = true
        override val position: Vector3f get() = Vector3f()
        override val rotation: Quaternion get() = Quaternion.Identity
        override val velocity: Vector3f get() = Vector3f()
    }

    companion object {
        fun create(pos: Vector3f = Vector3f(), rotation: Quaternion = Quaternion.Identity): Transform =
            object : Transform {
                override val parent: Transform? get() = null
                override val isValid: Boolean get() = true
                override val position: Vector3f get() = pos
                override val rotation: Quaternion get() = rotation
                override val velocity: Vector3f get() = Vector3f()
            }
    }
}