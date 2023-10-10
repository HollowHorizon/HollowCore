package ru.hollowhorizon.hc.client.utils.math

import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Vector3f
import com.mojang.math.Vector4f
import ru.hollowhorizon.hc.client.utils.RGBA

/**
 * [Vector3f] extensions
 */

//variables
var Vector3f.x: Float
    get() = this.x()
    set(value) = this.setX(value)
var Vector3f.y: Float
    get() = this.y()
    set(value) = this.setY(value)
var Vector3f.z: Float
    get() = this.z()
    set(value) = this.setZ(value)

//methods
operator fun Vector3f.plusAssign(vector: Vector3f) {
    this.add(vector)
}

operator fun Vector3f.minusAssign(vector: Vector3f) {
    this.sub(vector)
}

operator fun Vector3f.timesAssign(vector: Vector3f) {
    this.mul(vector.x(), vector.y(), vector.z())
}

operator fun Vector3f.timesAssign(scale: Float) {
    this.mul(scale)
}

operator fun Vector3f.plus(vector: Vector3f): Vector3f {
    return Vector3f(this.x() + vector.x(), this.y() + vector.y(), this.z() + vector.z())
}

operator fun Vector3f.minus(vector: Vector3f): Vector3f {
    return Vector3f(this.x() - vector.x(), this.y() - vector.y(), this.z() - vector.z())
}

operator fun Vector3f.times(vector: Vector3f): Vector3f {
    return Vector3f(this.x() * vector.x(), this.y() * vector.y(), this.z() * vector.z())
}

operator fun Vector3f.times(scale: Float): Vector3f {
    return Vector3f(this.x() * scale, this.y() * scale, this.z() * scale)
}

operator fun Vector3f.unaryMinus(): Vector3f {
    return Vector3f(-this.x(), -this.y(), -this.z())
}

operator fun Vector4f.plusAssign(vector: Vector4f) {
    this.add(vector.x(), vector.y(), vector.z(), vector.w())
}

operator fun Vector4f.minusAssign(vector: Vector4f) {
    this.setX(x() - vector.x())
    this.setY(y() - vector.y())
    this.setZ(z() - vector.z())
    this.setW(w() - vector.w())
}

operator fun Vector4f.timesAssign(vector: Vector4f) {
    this.setX(x() * vector.x())
    this.setY(y() * vector.y())
    this.setZ(z() * vector.z())
    this.setW(w() * vector.w())
}

operator fun Vector4f.timesAssign(scale: Float) {
    this.mul(scale)
}

operator fun Vector4f.plus(vector: Vector4f): Vector4f {
    return Vector4f(this.x() + vector.x(), this.y() + vector.y(), this.z() + vector.z(), this.w() + vector.w())
}

operator fun Vector4f.minus(vector: Vector4f): Vector4f {
    return Vector4f(this.x() - vector.x(), this.y() - vector.y(), this.z() - vector.z(), this.w() - vector.w())
}

operator fun Vector4f.times(vector: Vector4f): Vector4f {
    return Vector4f(this.x() * vector.x(), this.y() * vector.y(), this.z() * vector.z(), this.w() * vector.w())
}

operator fun Vector4f.times(scale: Float): Vector4f {
    return Vector4f(this.x() * scale, this.y() * scale, this.z() * scale, this.w() * scale)
}

operator fun Vector4f.unaryMinus(): Vector4f {
    return Vector4f(-this.x(), -this.y(), -this.z(), -this.w())
}

/**
 * [IVertexBuilder] extensions
 */

operator fun BufferBuilder.plusAssign(v: VertexExt) {
    this.vertex(v.stack.last().pose(), v.x.toFloat(), v.y.toFloat(), v.z.toFloat())
    this.color(v.rgba.r, v.rgba.g, v.rgba.b, v.rgba.a)
    this.endVertex()
}

class VertexExt(val stack: PoseStack, val x: Int, val y: Int, val z: Int, val rgba: RGBA)