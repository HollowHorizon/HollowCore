package ru.hollowhorizon.hc.client.models.gltf.manager

import net.minecraft.world.entity.Entity
import ru.hollowhorizon.hc.client.models.gltf.animations.PlayType
import ru.hollowhorizon.hc.client.utils.isLogicalClient

interface IModelManager {
    companion object {
        fun <T> create(entity: T): IModelManager where T : IAnimated, T : Entity {
            return if (isLogicalClient) ClientModelManager(
                GltfManager.getOrCreate(entity.model)
            )
            else ServerModelManager(entity)
        }
    }

    fun startAnimation(name: String, priority: Float = 1.0f, playType: PlayType = PlayType.ONCE, speed: Float = 1.0f)

    fun stopAnimation(name: String)

}