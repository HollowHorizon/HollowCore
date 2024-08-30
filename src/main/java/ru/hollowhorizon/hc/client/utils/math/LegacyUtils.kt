//? if <=1.19.2 {
/*package ru.hollowhorizon.hc.client.utils.math

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.math.Quaternion
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import org.joml.Matrix3f
import org.joml.Matrix4f
import org.joml.Quaternionf
import ru.hollowhorizon.hc.client.utils.toMc

fun PoseStack.translate(x: Float, y: Float, z: Float) = translate(x.toDouble(), y.toDouble(), z.toDouble())
fun PoseStack.mulPoseMatrix(matrix: Matrix4f) = mulPoseMatrix(matrix.toMc())
fun PoseStack.mulPose(quat: Quaternionf) = mulPose(Quaternion(quat.x, quat.y, quat.z, quat.w))
fun VertexConsumer.vertex(matrix: Matrix4f, x: Float, y: Float, z: Float): VertexConsumer {
    this.vertex(matrix.toMc(), x, y, z)
    return this
}

fun com.mojang.math.Matrix3f.mul(matrix: Matrix3f) = mul(matrix.toMc())
fun Entity.level(): Level = level
fun ServerPlayer.serverLevel() = getLevel()
*///?}