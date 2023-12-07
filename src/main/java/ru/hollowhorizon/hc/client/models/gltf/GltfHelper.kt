package ru.hollowhorizon.hc.client.models.gltf

import org.lwjgl.BufferUtils
import ru.hollowhorizon.hc.client.utils.math.array
import java.nio.FloatBuffer
import java.util.*
import kotlin.math.abs


var uniformFloatBuffer: FloatBuffer? = null

val GltfTree.Node.canHaveHardwareSkinning: Boolean
    get() = mesh?.primitives?.any { it.attributes.containsKey(GltfAttribute.JOINTS_1) } ?: false

fun mul4x4(a: FloatArray, b: FloatArray, m: FloatArray) {
    val a00 = a[0]
    val a10 = a[1]
    val a20 = a[2]
    val a30 = a[3]
    val a01 = a[4]
    val a11 = a[5]
    val a21 = a[6]
    val a31 = a[7]
    val a02 = a[8]
    val a12 = a[9]
    val a22 = a[10]
    val a32 = a[11]
    val a03 = a[12]
    val a13 = a[13]
    val a23 = a[14]
    val a33 = a[15]

    val b00 = b[0]
    val b10 = b[1]
    val b20 = b[2]
    val b30 = b[3]
    val b01 = b[4]
    val b11 = b[5]
    val b21 = b[6]
    val b31 = b[7]
    val b02 = b[8]
    val b12 = b[9]
    val b22 = b[10]
    val b32 = b[11]
    val b03 = b[12]
    val b13 = b[13]
    val b23 = b[14]
    val b33 = b[15]

    val m00 = a00 * b00 + a01 * b10 + a02 * b20 + a03 * b30
    val m01 = a00 * b01 + a01 * b11 + a02 * b21 + a03 * b31
    val m02 = a00 * b02 + a01 * b12 + a02 * b22 + a03 * b32
    val m03 = a00 * b03 + a01 * b13 + a02 * b23 + a03 * b33
    val m10 = a10 * b00 + a11 * b10 + a12 * b20 + a13 * b30
    val m11 = a10 * b01 + a11 * b11 + a12 * b21 + a13 * b31
    val m12 = a10 * b02 + a11 * b12 + a12 * b22 + a13 * b32
    val m13 = a10 * b03 + a11 * b13 + a12 * b23 + a13 * b33
    val m20 = a20 * b00 + a21 * b10 + a22 * b20 + a23 * b30
    val m21 = a20 * b01 + a21 * b11 + a22 * b21 + a23 * b31
    val m22 = a20 * b02 + a21 * b12 + a22 * b22 + a23 * b32
    val m23 = a20 * b03 + a21 * b13 + a22 * b23 + a23 * b33
    val m30 = a30 * b00 + a31 * b10 + a32 * b20 + a33 * b30
    val m31 = a30 * b01 + a31 * b11 + a32 * b21 + a33 * b31
    val m32 = a30 * b02 + a31 * b12 + a32 * b22 + a33 * b32
    val m33 = a30 * b03 + a31 * b13 + a32 * b23 + a33 * b33

    m[0] = m00
    m[1] = m10
    m[2] = m20
    m[3] = m30
    m[4] = m01
    m[5] = m11
    m[6] = m21
    m[7] = m31
    m[8] = m02
    m[9] = m12
    m[10] = m22
    m[11] = m32
    m[12] = m03
    m[13] = m13
    m[14] = m23
    m[15] = m33
}
private const val FLOAT_EPSILON = 1e-8f
fun invert4x4(m: FloatArray, inv: FloatArray) {
    val m0 = m[0]
    val m1 = m[1]
    val m2 = m[2]
    val m3 = m[3]
    val m4 = m[4]
    val m5 = m[5]
    val m6 = m[6]
    val m7 = m[7]
    val m8 = m[8]
    val m9 = m[9]
    val mA = m[10]
    val mB = m[11]
    val mC = m[12]
    val mD = m[13]
    val mE = m[14]
    val mF = m[15]
    inv[0] = m5 * mA * mF - m5 * mB * mE - m9 * m6 * mF + m9 * m7 * mE + mD * m6 * mB - mD * m7 * mA
    inv[4] = -m4 * mA * mF + m4 * mB * mE + m8 * m6 * mF - m8 * m7 * mE - mC * m6 * mB + mC * m7 * mA
    inv[8] = m4 * m9 * mF - m4 * mB * mD - m8 * m5 * mF + m8 * m7 * mD + mC * m5 * mB - mC * m7 * m9
    inv[12] = -m4 * m9 * mE + m4 * mA * mD + m8 * m5 * mE - m8 * m6 * mD - mC * m5 * mA + mC * m6 * m9
    inv[1] = -m1 * mA * mF + m1 * mB * mE + m9 * m2 * mF - m9 * m3 * mE - mD * m2 * mB + mD * m3 * mA
    inv[5] = m0 * mA * mF - m0 * mB * mE - m8 * m2 * mF + m8 * m3 * mE + mC * m2 * mB - mC * m3 * mA
    inv[9] = -m0 * m9 * mF + m0 * mB * mD + m8 * m1 * mF - m8 * m3 * mD - mC * m1 * mB + mC * m3 * m9
    inv[13] = m0 * m9 * mE - m0 * mA * mD - m8 * m1 * mE + m8 * m2 * mD + mC * m1 * mA - mC * m2 * m9
    inv[2] = m1 * m6 * mF - m1 * m7 * mE - m5 * m2 * mF + m5 * m3 * mE + mD * m2 * m7 - mD * m3 * m6
    inv[6] = -m0 * m6 * mF + m0 * m7 * mE + m4 * m2 * mF - m4 * m3 * mE - mC * m2 * m7 + mC * m3 * m6
    inv[10] = m0 * m5 * mF - m0 * m7 * mD - m4 * m1 * mF + m4 * m3 * mD + mC * m1 * m7 - mC * m3 * m5
    inv[14] = -m0 * m5 * mE + m0 * m6 * mD + m4 * m1 * mE - m4 * m2 * mD - mC * m1 * m6 + mC * m2 * m5
    inv[3] = -m1 * m6 * mB + m1 * m7 * mA + m5 * m2 * mB - m5 * m3 * mA - m9 * m2 * m7 + m9 * m3 * m6
    inv[7] = m0 * m6 * mB - m0 * m7 * mA - m4 * m2 * mB + m4 * m3 * mA + m8 * m2 * m7 - m8 * m3 * m6
    inv[11] = -m0 * m5 * mB + m0 * m7 * m9 + m4 * m1 * mB - m4 * m3 * m9 - m8 * m1 * m7 + m8 * m3 * m5
    inv[15] = m0 * m5 * mA - m0 * m6 * m9 - m4 * m1 * mA + m4 * m2 * m9 + m8 * m1 * m6 - m8 * m2 * m5
    val det = m0 * inv[0] + m1 * inv[4] + m2 * inv[8] + m3 * inv[12]
    if (abs(det) <= FLOAT_EPSILON) {
        setIdentity4x4(inv)
        return
    }
    val invDet = 1.0f / det
    for (i in 0..15) {
        inv[i] *= invDet
    }
}

fun setIdentity4x4(m: FloatArray) {
    Arrays.fill(m, 0.0f)
    m[0] = 1.0f
    m[5] = 1.0f
    m[10] = 1.0f
    m[15] = 1.0f
}

val FloatArray.floatBuffer: FloatBuffer
    get() {
        val total: Int = this.size
        if (uniformFloatBuffer == null || uniformFloatBuffer!!.capacity() < total) {
            uniformFloatBuffer = BufferUtils.createFloatBuffer(total)
        }
        uniformFloatBuffer!!.position(0)
        uniformFloatBuffer!!.limit(uniformFloatBuffer!!.capacity())
        uniformFloatBuffer!!.put(this)
        uniformFloatBuffer!!.flip()
        return uniformFloatBuffer!!
    }