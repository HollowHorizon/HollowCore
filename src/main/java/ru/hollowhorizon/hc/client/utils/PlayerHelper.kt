package ru.hollowhorizon.hc.client.utils

import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.levelgen.Heightmap
import net.minecraft.world.phys.Vec3
import kotlin.math.abs
import kotlin.random.Random

tailrec fun Player.findRandomPos(radius: Int): Vec3 {
    val distance = Random.nextDouble(1.0, radius.toDouble())
    val rotation = Random.nextDouble(0.0, 2.0*Math.PI).toFloat()

    val pos = position().add(
        distance * Mth.cos(rotation),
        0.0,
        distance * Mth.sin(rotation)
    )

    val angle = Mth.atan2(pos.z - z, pos.x - x) * 180 / Mth.PI
    val normalized = normalizeAngle(angle - yHeadRot)

    return if(abs(normalized) > 60.0) {
        val block = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE_WG, BlockPos(pos))
        Vec3(pos.x, block.y.toDouble(), pos.z)
    } else findRandomPos(radius)
}

fun normalizeAngle(angle: Double): Double {
    return (angle + 2 * Math.PI) % (2 * Math.PI)
}