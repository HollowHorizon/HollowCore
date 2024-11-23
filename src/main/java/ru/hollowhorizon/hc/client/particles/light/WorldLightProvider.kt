package ru.hollowhorizon.hc.client.particles.light

import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.LightTexture
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import org.joml.Vector3f

class WorldLightProvider(private val world: Level) : LightProvider {
    override fun query(pos: Vector3f): Int {
        val block = with(pos.floor()) { BlockPos(x.toInt(), y.toInt(), z.toInt()) }
        if (!world.isLoaded(block)) return LightTexture.FULL_BRIGHT

        return LevelRenderer.getLightColor(world, block)
    }
}