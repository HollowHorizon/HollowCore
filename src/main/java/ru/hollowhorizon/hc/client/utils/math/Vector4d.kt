package ru.hollowhorizon.hc.client.utils.math

class Vector4d(
    var x: Double = 0.0,
    var y: Double = 0.0,
    var z: Double = 0.0,
    var w: Double = 0.0,
) {
    fun mulAffine(mat: Matrix4d, dest: Vector4d): Vector4d {
        val rx = mat.m00 * x + (mat.m10 * y + (mat.m20 * z + mat.m30 * w))
        val ry = mat.m01 * x + (mat.m11 * y + (mat.m21 * z + mat.m31 * w))
        val rz = mat.m02 * x + (mat.m12 * y + (mat.m22 * z + mat.m32 * w))
        dest.x = rx
        dest.y = ry
        dest.z = rz
        dest.w = w
        return dest
    }

    fun mulGeneric(mat: Matrix4d, dest: Vector4d): Vector4d {
        val rx = mat.m00 * x + (mat.m10 * y + (mat.m20 * z + mat.m30 * w))
        val ry = mat.m01 * x + (mat.m11 * y + (mat.m21 * z + mat.m31 * w))
        val rz = mat.m02 * x + (mat.m12 * y + (mat.m22 * z + mat.m32 * w))
        val rw = mat.m03 * x + (mat.m13 * y + (mat.m23 * z + mat.m33 * w))
        dest.x = rx
        dest.y = ry
        dest.z = rz
        dest.w = rw
        return dest
    }

    fun mul(mat: Matrix4d, dest: Vector4d): Vector4d {
        return if (mat.properties and Matrix4d.PROPERTY_AFFINE != 0) {
            mulAffine(mat, dest)
        } else {
            mulGeneric(mat, dest)
        }
    }

    fun x() = x
    fun y() = y
    fun z() = z
    fun w() = w

}