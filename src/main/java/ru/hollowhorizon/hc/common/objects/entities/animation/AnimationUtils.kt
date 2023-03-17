package ru.hollowhorizon.hc.common.objects.entities.animation

import net.minecraft.util.math.AxisAlignedBB
import ru.hollowhorizon.hc.client.models.core.animation.IPose

fun GetAABBForPose(pose: IPose): AxisAlignedBB {
    var highestX = 0.0
    var lowestX = 0.0
    var highestY = 0.0
    var highestZ = 0.0
    var lowestY = 0.0
    var lowestZ = 0.0
    for (i in 0 until pose.jointCount) {
        val workPos = pose.getJointMatrix(i).translation
        if (workPos.x() > highestX) {
            highestX = workPos.x()
        } else if (workPos.x() < lowestX) {
            lowestX = workPos.x()
        }
        if (workPos.y() > highestY) {
            highestY = workPos.y()
        } else if (workPos.y() < lowestY) {
            lowestY = workPos.y()
        }
        if (workPos.z() > highestZ) {
            highestZ = workPos.z()
        } else if (workPos.z() < lowestZ) {
            lowestZ = workPos.z()
        }
    }
    return AxisAlignedBB(lowestX, lowestY, lowestZ, highestX, highestY, highestZ)
}