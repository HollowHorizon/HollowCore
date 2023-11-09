package ru.hollowhorizon.hc.client.models.fbx

import com.mojang.math.Vector3f

class FBXCurveNode(val x: FBXKeyFrame?, val y: FBXKeyFrame?, val z: FBXKeyFrame?, @JvmField val type: CurveType, modelId: Long) {
    @JvmField
    val modelId: Long
    var currentX = 0f
        private set
    var currentY = 0f
        private set
    var currentZ = 0f
        private set

    init {
        when (type) {
            CurveType.SCALING -> {
                currentX = 1f
                currentY = 1f
                currentZ = 1f
            }

            CurveType.TRANSLATION, CurveType.ROTATION -> {
                currentX = 0f
                currentY = 0f
                currentZ = 0f
            }
        }
        this.modelId = modelId
    }

    val currentVector: Vector3f
        get() = Vector3f(currentX, currentY, currentZ)

    fun updateValues(frame: Int): Boolean {
        if (x != null) {
            if (frame > x.values.size - 1) return true
            currentX = x.values[frame]
        }
        if (y != null) {
            if (frame > y.values.size - 1) return true
            currentY = y.values[frame]
        }
        if (z != null) {
            if (frame > z.values.size - 1) return true
            currentZ = z.values[frame]
        }
        return false
    }

    enum class CurveType {
        TRANSLATION,
        ROTATION,
        SCALING
    }
}
