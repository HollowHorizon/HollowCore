package ru.hollowhorizon.hc.client.gltf.animations.manager

import com.modularmods.mcgltf.MCglTF
import net.minecraft.world.entity.Entity
import net.minecraftforge.fml.loading.FMLEnvironment
import ru.hollowhorizon.hc.client.gltf.IAnimated
import ru.hollowhorizon.hc.client.gltf.animations.PlayType

interface IAnimationManager {
    companion object {
        fun <T> create(entity: T): IAnimationManager where T : IAnimated, T : Entity {
            return if (FMLEnvironment.dist.isClient) ClientAnimationManager(MCglTF.getOrCreate(entity.model))
            else ServerAnimationManager(entity)
        }
    }

    fun startAnimation(name: String, priority: Float = 1.0f, playType: PlayType = PlayType.ONCE, speed: Float = 1.0f)

    fun stopAnimation(name: String)
}