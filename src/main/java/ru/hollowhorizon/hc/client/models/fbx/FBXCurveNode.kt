/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.models.fbx

import org.joml.Vector3f


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
