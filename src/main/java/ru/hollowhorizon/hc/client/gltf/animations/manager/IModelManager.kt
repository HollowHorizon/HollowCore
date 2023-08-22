package ru.hollowhorizon.hc.client.gltf.animations.manager

import com.modularmods.mcgltf.MCglTF
import net.minecraft.world.entity.Entity
import ru.hollowhorizon.hc.client.gltf.IAnimated
import ru.hollowhorizon.hc.client.gltf.Transform
import ru.hollowhorizon.hc.client.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.gltf.animations.PlayType
import ru.hollowhorizon.hc.client.utils.isLogicalClient

interface IModelManager {
    var transform: Transform

    companion object {
        fun <T> create(entity: T): IModelManager where T : IAnimated, T : Entity {
            return if (isLogicalClient) ClientModelManager(
                MCglTF.getOrCreate(entity.model)
            )
            else ServerModelManager(entity)
        }
    }

    fun startAnimation(name: String, priority: Float = 1.0f, playType: PlayType = PlayType.ONCE, speed: Float = 1.0f)

    fun stopAnimation(name: String)

    fun setDefaultAnimations(animations: Map<AnimationType, String>)
}