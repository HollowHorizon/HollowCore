package ru.hollowhorizon.hc.client.utils.math


class CubicSpline {
    private var lastIndex: Int = 0
    private lateinit var a: FloatArray
    private lateinit var b: FloatArray

    private lateinit var xOrig: FloatArray
    private lateinit var yOrig: FloatArray

    fun fitAndEval(
        x: FloatArray,
        y: FloatArray,
        xs: FloatArray,
        startSlope: Float = Float.NaN,
        endSlope: Float = Float.NaN,
    ): FloatArray {
        fit(x, y, startSlope, endSlope)
        return eval(xs)
    }

    fun fit(
        x: FloatArray,
        y: FloatArray,
        startSlope: Float = Float.NaN,
        endSlope: Float = Float.NaN,
    ) {
        if (startSlope.isInfinite() || endSlope.isInfinite()) {
            throw IllegalStateException("startSlope and endSlope cannot be infinity.")
        }

        this.xOrig = x
        this.yOrig = y

        val n = x.size
        val r = FloatArray(n)
        val m = TriDiagonalMatrixF(n)
        var dx1: Float
        var dx2: Float
        var dy1: Float
        var dy2: Float

        if (startSlope.isNaN()) {
            dx1 = x[1] - x[0]
            m.C[0] = 1.0f / dx1
            m.B[0] = 2.0f * m.C[0]
            r[0] = 3 * (y[1] - y[0]) / (dx1 * dx1)
        } else {
            m.B[0] = 1f
            r[0] = startSlope
        }

        for (i in 1 until n - 1) {
            dx1 = x[i] - x[i - 1]
            dx2 = x[i + 1] - x[i]
            m.A[i] = 1.0f / dx1
            m.C[i] = 1.0f / dx2
            m.B[i] = 2.0f * (m.A[i] + m.C[i])
            dy1 = y[i] - y[i - 1]
            dy2 = y[i + 1] - y[i]
            r[i] = 3 * (dy1 / (dx1 * dx1) + dy2 / (dx2 * dx2))
        }

        if (endSlope.isNaN()) {
            dx1 = x[n - 1] - x[n - 2]
            dy1 = y[n - 1] - y[n - 2]
            m.A[n - 1] = 1.0f / dx1
            m.B[n - 1] = 2.0f * m.A[n - 1]
            r[n - 1] = 3 * (dy1 / (dx1 * dx1))
        } else {
            m.B[n - 1] = 1f
            r[n - 1] = endSlope
        }

        val k: FloatArray = m.solve(r)

        this.a = FloatArray(n - 1)
        this.b = FloatArray(n - 1)

        for (i in 1 until n) {
            dx1 = x[i] - x[i - 1]
            dy1 = y[i] - y[i - 1]
            a[i - 1] = k[i - 1] * dx1 - dy1
            b[i - 1] = -k[i] * dx1 + dy1
        }
    }


    private fun eval(x: FloatArray): FloatArray {

        val n = x.size
        val y = FloatArray(n)

        lastIndex = 0
        for (i in 0 until n) {
            val j = GetNextXIndex(x[i])

            y[i] = EvalSpline(x[i], j)
        }

        return y
    }

    private fun EvalSpline(x: Float, j: Int): Float {
        val dx = xOrig[j + 1] - xOrig[j]
        val t = (x - xOrig[j]) / dx
        return (1 - t) * yOrig[j] + t * yOrig[j + 1] + t * (1 - t) * (a[j] * (1 - t) + b[j] * t)

    }

    private fun GetNextXIndex(x: Float): Int {
        if (x < xOrig[lastIndex]) {
            throw IllegalArgumentException("The X values to evaluate must be sorted.")
        }
        while (lastIndex < xOrig.size - 2 && x > xOrig[lastIndex + 1]) {
            lastIndex++
        }
        return lastIndex
    }

    class TriDiagonalMatrixF(n: Int) {
        var A: FloatArray = FloatArray(n)

        var B: FloatArray = FloatArray(n)

        var C: FloatArray = FloatArray(n)

        fun getN(): Int {
            return A.size
        }

        fun get(row: Int, col: Int): Float {
            return when (row - col) {
                0 -> {
                    B[row]
                }

                -1 -> {
                    assert(row < getN() - 1)
                    C[row]
                }

                1 -> {
                    assert(row > 0)
                    A[row]
                }

                else -> {
                    0f
                }
            }
        }

        fun set(row: Int, col: Int, value: Float) {
            when (row - col) {
                0 -> {
                    B[row] = value
                }

                -1 -> {
                    assert(row < getN() - 1)
                    C[row] = value
                }

                1 -> {
                    assert(row > 0)
                    A[row] = value
                }

                else -> {
                    throw IllegalArgumentException("Only the main, super, and sub diagonals can be set.")
                }
            }
        }

        fun solve(d: FloatArray): FloatArray {
            val n = getN()
            require(d.size == n) { "The input d is not the same size as this matrix." }

            // cPrime
            val cPrime = FloatArray(n)
            cPrime[0] = C[0] / B[0]
            for (i in 1 until n) {
                cPrime[i] = C[i] / (B[i] - cPrime[i - 1] * A[i])
            }

            // dPrime
            val dPrime = FloatArray(n)
            dPrime[0] = d[0] / B[0]
            for (i in 1 until n) {
                dPrime[i] = (d[i] - dPrime[i - 1] * A[i]) / (B[i] - cPrime[i - 1] * A[i])
            }

            // Back substitution
            val x = FloatArray(n)
            x[n - 1] = dPrime[n - 1]
            for (i in n - 2 downTo 0) {
                x[i] = dPrime[i] - cPrime[i] * x[i + 1]
            }
            return x
        }
    }
}