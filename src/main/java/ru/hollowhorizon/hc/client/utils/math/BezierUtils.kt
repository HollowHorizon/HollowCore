package ru.hollowhorizon.hc.client.utils.math

import net.minecraft.util.math.vector.Vector3d


object BezierUtils {
    fun calculateSpline(
        x: FloatArray,
        y: FloatArray,
        z: FloatArray,
        n: Int
    ): List<Vec3d> {

        val ys = FloatArray(n)
        val stepSize: Float = ((y[y.size - 1] - y[0]) / (n - 1))

        for (i in 0 until n) {
            ys[i] = y[0] + i * stepSize
        }

        val spline = CubicSpline()
        val xs: FloatArray = spline.fitAndEval(y, x, ys)
        val zs: FloatArray = spline.fitAndEval(y, z, ys)

        return createList(
            xs.map { it.toDouble() }.toList(),
            ys.map { it.toDouble() }.toList(),
            zs.map { it.toDouble() }.toList()
        )
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