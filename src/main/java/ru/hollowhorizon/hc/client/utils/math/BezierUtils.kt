package ru.hollowhorizon.hc.client.utils.math

import com.mojang.math.Vector3d


object BezierUtils {
    fun calculateSpline(
        list: List<Vector3d>,
        n: Int
    ): List<Vector3d> {
        return smoothCurve(list, n)
    }


    fun bezierCurve(points: List<Vector3d>, t: Double): Vector3d {
        return if (points.size == 1) {
            points[0]
        } else {
            val newPoints = mutableListOf<Vector3d>()
            for (i in 0 until points.size - 1) {
                val p1 = points[i]
                val p2 = points[i + 1]
                val x = (1 - t) * p1.x + t * p2.x
                val y = (1 - t) * p1.y + t * p2.y
                val z = (1 - t) * p1.z + t * p2.z
                newPoints.add(Vector3d(x, y, z))
            }
            bezierCurve(newPoints, t)
        }
    }

    fun smoothCurve(points: List<Vector3d>, resolution: Int): List<Vector3d> {
        val curvePoints = mutableListOf<Vector3d>()
        for (i in 0 until resolution) {
            val t = i.toDouble() / (resolution - 1)
            curvePoints.add(bezierCurve(points, t))
        }
        return curvePoints
    }

    data class Vec3d(val x: Double, val y: Double, val z: Double)

    private fun createList(x: List<Double>, y: List<Double>, z: List<Double>): List<Vec3d> {
        val list = arrayListOf<Vec3d>()
        for (i in x.indices) {
            list.add(Vec3d(x[i], y[i], z[i]))
        }
        return list
    }

    private fun toXVector3d(v: List<Vector3d>): FloatArray {
        val values = FloatArray(v.size)
        for (i in v.indices) {
            values[i] = v[i].x.toFloat()
        }
        return values
    }

    private fun toYVector3d(v: List<Vector3d>): FloatArray {
        val values = FloatArray(v.size)
        for (i in v.indices) {
            values[i] = v[i].y.toFloat()
        }

        return values
    }

    private fun toZVector3d(v: List<Vector3d>): FloatArray {
        val values = FloatArray(v.size)
        for (i in v.indices) {
            values[i] = v[i].z.toFloat()
        }
        return values
    }
}