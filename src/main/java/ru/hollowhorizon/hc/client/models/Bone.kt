package ru.hollowhorizon.hc.client.models

import jassimp.AiNodeAnim
import net.minecraft.util.math.vector.Matrix4f
import net.minecraft.util.math.vector.Quaternion
import net.minecraft.util.math.vector.Vector3f


class Bone(val name: String, private val id: Int, node: AiNodeAnim) {
    private val positions: MutableList<KeyPosition> = ArrayList()
    private val rotations: MutableList<KeyRotation> = ArrayList()
    private val scales: MutableList<KeyScale> = ArrayList()
    private var localTransformation = Matrix4f().apply { setIdentity() }

    init {
        val numPositions = node.numPosKeys
        for (i in 0 until numPositions) {
            val x = node.getPosKeyX(i).toDouble()
            val y = node.getPosKeyY(i).toDouble()
            val z = node.getPosKeyZ(i).toDouble()
            val timeStamp = node.getPosKeyTime(i)
            positions.add(KeyPosition(Vector3f(x.toFloat(), y.toFloat(), z.toFloat()), timeStamp))
        }
        val numRotations = node.numRotKeys
        for (i in 0 until numRotations) {
            val x: Quaternion = node.getRotKeyQuaternion(i, AssimpJomlProvider())
            val timeStamp = node.getRotKeyTime(i)
            rotations.add(KeyRotation(x, timeStamp))
        }
        val numScales = node.numScaleKeys
        for (i in 0 until numScales) {
            val x = node.getScaleKeyX(i).toDouble()
            val y = node.getScaleKeyY(i).toDouble()
            val z = node.getScaleKeyZ(i).toDouble()
            val timeStamp = node.getScaleKeyTime(i)
            scales.add(KeyScale(Vector3f(x.toFloat(), y.toFloat(), z.toFloat()), timeStamp))
        }
        println("Bone: $name ID: $id NumPositions: $numPositions")
    }

    fun update(partialTicks: Double) {
        localTransformation = interpolatePos(partialTicks).apply {
            multiply(interpolateRotation(partialTicks))
            multiply(interpolateScale(partialTicks))
        }

        // Matrix4f.mul(interpolatePos(partialTicks), interpolateRotation(partialTicks), localTransformation);
    }

    private fun getScaleFactor(lastTimeStamp: Double, nextTimeStamp: Double, animationTime: Double): Float {
        val scaleFactor: Float
        val midWayLength = animationTime - lastTimeStamp
        val framesDiff = nextTimeStamp - lastTimeStamp
        scaleFactor = (midWayLength / framesDiff).toFloat()
        return scaleFactor
    }

    private fun interpolatePos(timestamp: Double): Matrix4f {
        if (positions.size == 1) {
            val position = positions[0]
            return Matrix4f().apply { translate(position.getPosition()) }
        }
        val pos = getPosition(timestamp)
        val pos1 = pos + 1
        val scaleFactor = getScaleFactor(
            positions[pos].timestamp,
            positions[pos1].timestamp, timestamp
        )
        val finalPosition: Vector3f = lerp(
            positions[pos].getPosition(),
            positions[pos1].getPosition(), scaleFactor
        )

        return Matrix4f().apply {
            setIdentity()
            translate(finalPosition)
        }
    }

    fun lerp(start: Vector3f, end: Vector3f, progression: Float): Vector3f {
        val x = start.x() + (end.x() - start.x()) * progression
        val y = start.y() + (end.y() - start.y()) * progression
        val z = start.z() + (end.z() - start.z()) * progression
        return Vector3f(x, y, z)
    }

    private fun interpolateRotation(timestamp: Double): Matrix4f {
        if (rotations.size == 1) {
            val rotation = rotations[0]
            return Matrix4f(rotation.getQuaternion().apply { normalize() })
        }
        val rot = getRotation(timestamp)
        val rot1 = rot + 1
        val scaleFactor = getScaleFactor(
            rotations[rot].timestamp,
            rotations[rot1].timestamp, timestamp
        )
        return Matrix4f(
            normalizedLerp(
                rotations[rot].getQuaternion(),
                rotations[rot1].getQuaternion(),
                scaleFactor
            )
        )
        // return rotations.get(rot).getQuaternion().slerp(rotations.get(rot1).getQuaternion(), scaleFactor, new Quaternionf()).get(new Matrix4f());
    }

    fun normalizedLerp(a: Quaternion, b: Quaternion, blend: Float): Quaternion {
        val result = Quaternion(0f, 0f, 0f, 1f)
        val dot: Float = a.r() * b.r() + a.i() * b.i() + a.j() * b.j() + a.k() * b.k()
        val blendI = 1f - blend
        if (dot < 0) {
            result.set(
                blendI * a.i() + blend * -b.i(),
                blendI * a.j() + blend * -b.j(),
                blendI * a.k() + blend * -b.k(),
                blendI * a.r() + blend * -b.r()
            )
        } else {
            result.set(
                blendI * a.i() + blend * b.i(),
                blendI * a.j() + blend * b.j(),
                blendI * a.k() + blend * b.k(),
                blendI * a.r() + blend * b.r()
            )
        }
        result.normalize()
        return result
    }

    private fun interpolateScale(timestamp: Double): Matrix4f {
        if (scales.size == 1) {
            val scale = scales[0]
            return Matrix4f().apply { translate(scale.getScale()) }
        }
        val scale = getScale(timestamp)
        val scale1 = scale + 1
        val scaleFactor = getScaleFactor(
            scales[scale].timestamp,
            scales[scale1].timestamp, timestamp
        )
        val finalScale: Vector3f = mix(scales[scale].getScale(), scales[scale1].getScale(), scaleFactor)
        return Matrix4f().apply {
            setIdentity()
            m00 = finalScale.x()
            m11 = finalScale.y()
            m22 = finalScale.z()
        }
    }

    fun mix(first: Vector3f, second: Vector3f, factor: Float): Vector3f {
        return Vector3f(
            first.x() * (1.0f - factor) + second.x() * factor,
            first.y() * (1.0f - factor) + second.y() * factor,
            first.z() * (1.0f - factor) + second.z() * factor
        )
    }

    fun getPosition(timestamp: Double): Int {
        for (i in positions.indices) {
            if (positions[i].timestamp >= timestamp) return i
        }
        return -1
    }

    fun getRotation(timestamp: Double): Int {
        for (i in rotations.indices) {
            if (rotations[i].timestamp >= timestamp) return i
        }
        return -1
    }

    fun getScale(timestamp: Double): Int {
        for (i in scales.indices) {
            if (scales[i].timestamp >= timestamp) return i
        }
        return -1
    }

    fun getLocalTransformation(): Matrix4f {
        return localTransformation
    }

    internal class KeyPosition(position: Vector3f, timestamp: Double) {
        private val position: Vector3f
        val timestamp: Double

        init {
            this.position = position
            this.timestamp = timestamp
        }

        fun getPosition(): Vector3f {
            return position
        }
    }

    internal class KeyRotation(quaternion: Quaternion, timestamp: Double) {
        private val quaternion: Quaternion
        val timestamp: Double

        init {
            this.quaternion = quaternion
            this.timestamp = timestamp
        }

        fun getQuaternion(): Quaternion {
            return quaternion
        }
    }

    internal class KeyScale(scale: Vector3f, timestamp: Double) {
        private val scale: Vector3f
        val timestamp: Double

        init {
            this.scale = scale
            this.timestamp = timestamp
        }

        fun getScale(): Vector3f {
            return scale
        }
    }
}