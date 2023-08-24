package ru.hollowhorizon.hc.client.gltf.animations.manager

import net.minecraft.world.entity.Entity
import net.minecraftforge.network.PacketDistributor
import ru.hollowhorizon.hc.client.gltf.Transform
import ru.hollowhorizon.hc.client.gltf.animations.AnimationType
import ru.hollowhorizon.hc.client.gltf.animations.PlayType
import ru.hollowhorizon.hc.common.network.packets.*
import ru.hollowhorizon.hc.common.network.send

class ServerModelManager(val entity: Entity) : IModelManager {

    override fun startAnimation(name: String, priority: Float, playType: PlayType, speed: Float) {
        StartAnimationPacket().send(
            StartAnimationContainer(entity.id, name, priority, playType, speed),
            PacketDistributor.TRACKING_ENTITY.with { entity })
    }

    override fun stopAnimation(name: String) {
        StopAnimationPacket().send(
            StopAnimationContainer(entity.id, name),
            PacketDistributor.TRACKING_ENTITY.with { entity })
    }

}
