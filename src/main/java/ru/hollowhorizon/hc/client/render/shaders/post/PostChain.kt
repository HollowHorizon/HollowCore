/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.render.shaders.post

import com.mojang.blaze3d.systems.RenderSystem
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.client.utils.nbt.ForResourceLocation
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3

//TODO: Добавить настройки uniform'ов для каждого шейдера
//TODO: Сделать систему не зависимой от GameRenderer::loadEffect
object PostChain : ResourceManagerReloadListener {
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

    override fun onResourceManagerReload(manager: ResourceManager) {
        manager.listResources("shaders") {
            it.path.endsWith(".post.fsh")
        }.forEach {
            val namespace = it.key.namespace
            val path = it.key.path.substringAfterLast("/").removeSuffix(".fsh")
            HollowPack.generatePostShader(ResourceLocation(namespace, path))
        }
    }
}

@Serializable
@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
class PostChainPacket(val location: @Serializable(ForResourceLocation::class) ResourceLocation?) :
    HollowPacketV3<PostChainPacket> {
    override fun handle(player: Player, data: PostChainPacket) {
        RenderSystem.recordRenderCall {
            if (location != null) PostChain.apply(location)
            else PostChain.shutdown()
        }
    }
}