package ru.hollowhorizon.hc.client.gltf.animations.manager

import ru.hollowhorizon.hc.client.gltf.model.GltfManager
import net.minecraft.world.entity.Entity
import ru.hollowhorizon.hc.client.gltf.IAnimated
import ru.hollowhorizon.hc.client.gltf.animations.PlayType
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