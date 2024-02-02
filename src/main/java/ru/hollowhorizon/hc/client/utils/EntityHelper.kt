package ru.hollowhorizon.hc.client.utils

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import java.util.function.Predicate
import kotlin.math.abs
import kotlin.random.Random


tailrec fun Player.findRandomPos(radius: Int): Vec3 {
    val distance = Random.nextDouble(1.0, radius.toDouble())
    val rotation = Random.nextDouble(0.0, 2.0 * Math.PI).toFloat()

    val pos = position().add(
        distance * Mth.cos(rotation),
        0.0,
        distance * Mth.sin(rotation)
    )

    val angle = Mth.atan2(pos.z - z, pos.x - x) * 180 / Mth.PI
    val normalized = normalizeAngle(angle - yHeadRot)

    return if (abs(normalized) > 60.0) {
        val block = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, BlockPos(pos))
        Vec3(pos.x, block.y.toDouble(), pos.z)
    } else findRandomPos(radius)
}

fun normalizeAngle(angle: Double): Double {
    return (angle + 2 * Math.PI) % (2 * Math.PI)
}

fun isInFrontOfEntity(entity: LivingEntity, target: Entity): Boolean {
    val vecTargetsPos: Vec3 = target.position()

    var vecFinal = vecTargetsPos.vectorTo(Vec3(entity.x, entity.y, entity.z)).normalize()
    vecFinal = Vec3(vecFinal.x, 0.0, vecFinal.z)
    return vecFinal.dot(entity.lookAngle) < 0.0
}

fun LivingEntity.isInSight(other: LivingEntity): Boolean {
    if (viewBlocked(this, other)) return false

    return isInFrontOfEntity(this, other)
}


private const val headSize = 0.15

fun viewBlocked(viewer: LivingEntity, other: LivingEntity): Boolean {
    val viewerBoundBox = viewer.boundingBox
    val otherBoundingBox = other.boundingBox
    val viewerPoints = arrayOf(
        Vec3(viewerBoundBox.minX, viewerBoundBox.minY, viewerBoundBox.minZ),
        Vec3(viewerBoundBox.minX, viewerBoundBox.minY, viewerBoundBox.maxZ),
        Vec3(viewerBoundBox.minX, viewerBoundBox.maxY, viewerBoundBox.minZ),
        Vec3(viewerBoundBox.minX, viewerBoundBox.maxY, viewerBoundBox.maxZ),
        Vec3(viewerBoundBox.maxX, viewerBoundBox.maxY, viewerBoundBox.minZ),
        Vec3(viewerBoundBox.maxX, viewerBoundBox.maxY, viewerBoundBox.maxZ),
        Vec3(viewerBoundBox.maxX, viewerBoundBox.minY, viewerBoundBox.maxZ),
        Vec3(viewerBoundBox.maxX, viewerBoundBox.minY, viewerBoundBox.minZ),
    )

    if (viewer is Player) {
        val pos = Vec3(viewer.getX(), viewer.getY() + 1.62f, viewer.getZ())
        viewerPoints[0] = pos.add(-headSize, -headSize, -headSize)
        viewerPoints[1] = pos.add(-headSize, -headSize, headSize)
        viewerPoints[2] = pos.add(-headSize, headSize, -headSize)
        viewerPoints[3] = pos.add(-headSize, headSize, headSize)
        viewerPoints[4] = pos.add(headSize, headSize, -headSize)
        viewerPoints[5] = pos.add(headSize, headSize, headSize)
        viewerPoints[6] = pos.add(headSize, -headSize, headSize)
        viewerPoints[7] = pos.add(headSize, -headSize, -headSize)
    }


    val otherPoints = arrayOf(
        Vec3(otherBoundingBox.minX, otherBoundingBox.minY, otherBoundingBox.minZ),
        Vec3(otherBoundingBox.minX, otherBoundingBox.minY, otherBoundingBox.maxZ),
        Vec3(otherBoundingBox.minX, otherBoundingBox.maxY, otherBoundingBox.minZ),
        Vec3(otherBoundingBox.minX, otherBoundingBox.maxY, otherBoundingBox.maxZ),
        Vec3(otherBoundingBox.maxX, otherBoundingBox.maxY, otherBoundingBox.minZ),
        Vec3(otherBoundingBox.maxX, otherBoundingBox.maxY, otherBoundingBox.maxZ),
        Vec3(otherBoundingBox.maxX, otherBoundingBox.minY, otherBoundingBox.maxZ),
        Vec3(otherBoundingBox.maxX, otherBoundingBox.minY, otherBoundingBox.minZ),
    )

    for (i in viewerPoints.indices) {
        if (viewer.level.clip(
                ClipContext(
                    viewerPoints[i],
                    otherPoints[i], ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, viewer
                )
            ).type == HitResult.Type.MISS
        ) return false
        if (rayTraceBlocks(viewer, viewer.level, viewerPoints[i], otherPoints[i]) { pos ->
                val state = viewer.level.getBlockState(pos)
                !canSeeThrough(state, viewer.level, pos)
            } == null) return false
    }

    return true
}

private fun rayTraceBlocks(
    livingEntity: LivingEntity,
    world: Level,
    vec1: Vec3,
    vec2: Vec3,
    stopOn: Predicate<BlockPos>,
): HitResult? {
    var mVec1 = vec1
    if (
        !java.lang.Double.isNaN(mVec1.x) && !java.lang.Double.isNaN(mVec1.y) && !java.lang.Double.isNaN(mVec1.z) &&
        !java.lang.Double.isNaN(vec2.x) && !java.lang.Double.isNaN(vec2.y) && !java.lang.Double.isNaN(vec2.z)
    ) {
        val i = Mth.floor(vec2.x)
        val j = Mth.floor(vec2.y)
        val k = Mth.floor(vec2.z)
        var l = Mth.floor(mVec1.x)
        var i1 = Mth.floor(mVec1.y)
        var j1 = Mth.floor(mVec1.z)
        var blockpos = BlockPos(l, i1, j1)
        if (stopOn.test(blockpos)) {
            return world.clip(
                ClipContext(
                    mVec1,
                    vec2,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    livingEntity
                )
            )
        }

        var k1 = 200

        while (k1-- >= 0) {
            if (java.lang.Double.isNaN(mVec1.x) || java.lang.Double.isNaN(mVec1.y) || java.lang.Double.isNaN(mVec1.z)) return null
            if (l == i && i1 == j && j1 == k) return null

            var flag2 = true
            var flag = true
            var flag1 = true
            var d0 = 999.0
            var d1 = 999.0
            var d2 = 999.0

            if (i > l) d0 = l.toDouble() + 1.0
            else if (i < l) d0 = l.toDouble() + 0.0
            else flag2 = false

            if (j > i1) d1 = i1.toDouble() + 1.0
            else if (j < i1) d1 = i1.toDouble() + 0.0
            else flag = false

            if (k > j1) d2 = j1.toDouble() + 1.0
            else if (k < j1) d2 = j1.toDouble() + 0.0
            else flag1 = false

            var d3 = 999.0
            var d4 = 999.0
            var d5 = 999.0
            val d6 = vec2.x - mVec1.x
            val d7 = vec2.y - mVec1.y
            val d8 = vec2.z - mVec1.z

            if (flag2) d3 = (d0 - mVec1.x) / d6
            if (flag) d4 = (d1 - mVec1.y) / d7
            if (flag1) d5 = (d2 - mVec1.z) / d8
            if (d3 == -0.0) d3 = -1.0E-4
            if (d4 == -0.0) d4 = -1.0E-4
            if (d5 == -0.0) d5 = -1.0E-4

            var enumfacing: Direction

            if (d3 < d4 && d3 < d5) {
                enumfacing = if (i > l) Direction.WEST else Direction.EAST
                mVec1 = Vec3(d0, mVec1.y + d7 * d3, mVec1.z + d8 * d3)
            } else if (d4 < d5) {
                enumfacing = if (j > i1) Direction.DOWN else Direction.UP
                mVec1 = Vec3(mVec1.x + d6 * d4, d1, mVec1.z + d8 * d4)
            } else {
                enumfacing = if (k > j1) Direction.NORTH else Direction.SOUTH
                mVec1 = Vec3(mVec1.x + d6 * d5, mVec1.y + d7 * d5, d2)
            }

            l = Mth.floor(mVec1.x) - (if (enumfacing === Direction.EAST) 1 else 0)
            i1 = Mth.floor(mVec1.y) - (if (enumfacing === Direction.UP) 1 else 0)
            j1 = Mth.floor(mVec1.z) - (if (enumfacing === Direction.SOUTH) 1 else 0)
            blockpos = BlockPos(l, i1, j1)
            if (stopOn.test(blockpos)) {
                return world.clip(
                    ClipContext(
                        mVec1,
                        vec2,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        livingEntity
                    )
                )
            }
        }
    }

    return null
}

fun canSeeThrough(blockState: BlockState, world: Level, pos: BlockPos): Boolean {
    if (!blockState.canOcclude() || !blockState.isSolidRender(world, pos)) return true

    val block = blockState.block

    // Special Snowflakes
    if (block is DoorBlock) return blockState.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER

    return blockState.getCollisionShape(world, pos) == Shapes.empty()
}