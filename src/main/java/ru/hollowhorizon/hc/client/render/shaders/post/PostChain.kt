package ru.hollowhorizon.hc.client.render.shaders.post

import com.mojang.blaze3d.systems.RenderSystem
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.world.entity.player.Player
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.client.utils.nbt.ForResourceLocation
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3

object PostChain {
    fun apply(location: ResourceLocation) {
        RenderSystem.recordRenderCall {
            Minecraft.getInstance().gameRenderer.loadEffect(location)
        }

    }

    fun shutdown() {
        RenderSystem.recordRenderCall {
            Minecraft.getInstance().gameRenderer.shutdownEffect()
        }
    }

    @JvmStatic
    fun onReload(event: RegisterClientReloadListenersEvent) {
        event.registerReloadListener(ResourceManagerReloadListener {
            it.listResources("shaders") {
                it.path.endsWith(".post.fsh")
            }.forEach {
                val namespace = it.key.namespace
                val path = it.key.path.substringAfterLast("/").removeSuffix(".fsh")
                HollowPack.getPackInstance().generatePostShader(ResourceLocation(namespace, path))
            }
        })
    }
}

@Serializable
@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
class PostChainPacket(val location: @Serializable(ForResourceLocation::class) ResourceLocation?): HollowPacketV3<PostChainPacket> {
    override fun handle(player: Player, data: PostChainPacket) {
        RenderSystem.recordRenderCall {
            if(location != null) PostChain.apply(location)
            else PostChain.shutdown()
        }
    }

}