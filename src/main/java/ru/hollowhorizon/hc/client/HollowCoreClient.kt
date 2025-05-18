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

package ru.hollowhorizon.hc.client

import com.mojang.blaze3d.systems.RenderSystem
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.modules.ui2.docking.UiDockable
import de.fabmax.kool.scene.Scene
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import org.lwjgl.glfw.GLFW
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.kool.*
import ru.hollowhorizon.hc.client.kool.minecraft.Image
import ru.hollowhorizon.hc.client.kool.minecraft.ImageManager
import ru.hollowhorizon.hc.client.models.internal.manager.GltfManager
import ru.hollowhorizon.hc.client.particles.BedrockParticles
import ru.hollowhorizon.hc.client.render.RenderManager
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer
import ru.hollowhorizon.hc.client.utils.HollowPack
import ru.hollowhorizon.hc.client.utils.open
import ru.hollowhorizon.hc.common.events.ClientOnly
import ru.hollowhorizon.hc.common.events.SubscribeEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterEntityRenderersEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterKeyBindingsEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterReloadListenersEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterResourcePacksEvent
import ru.hollowhorizon.hc.common.events.tick.TickEvent
import ru.hollowhorizon.hc.common.objects.entities.TestEntity
import ru.hollowhorizon.hc.common.registry.HollowModProcessor
import ru.hollowhorizon.hc.common.registry.ModEntities

@ClientOnly
object HollowCoreClient {

    init {
        HollowModProcessor.initMod()

        RenderSystem.recordRenderCall(RenderManager::onInitialize)
    }

    @SubscribeEvent
    fun onRegisterReloadListener(event: RegisterReloadListenersEvent.Client) {
        event.register(GltfManager)
        event.register(BedrockParticles)
        event.register(ImageManager)
    }

    @SubscribeEvent
    fun onRegisterResourcePacks(event: RegisterResourcePacksEvent) {
        event.addPack(HollowPack)
    }

    val KEY_V = KeyMapping("key.v", GLFW.GLFW_KEY_V, "key.v1")

    @SubscribeEvent
    fun onRegisterKeys(event: RegisterKeyBindingsEvent) {
        if (HollowCore.config.debugMode) event.registerKeyMapping(KEY_V)
    }

    @SubscribeEvent
    fun onClientTick(event: TickEvent.Client) {
        if(HollowCore.config.debugMode && KEY_V.isDown) {
            object: KoolScreen() {
                override fun Scene.setup() {
                    setupUiScene()

                    val window = UiDockable("Example")
                    addWindowSurface(window) {
                        Column(Grow.Std, Grow.Std) {
                            TitleBar(window)
                            Entity(TestEntity(ModEntities.TEST_ENTITY, Minecraft.getInstance().level!!)) {
                                modifier.size(Grow.Std, Grow.Std)
                            }
                        }
                        Image("hollowcore:textures/block/example.png")
                    }
                }
            }.open()
        }
    }

    @SubscribeEvent
    fun onEntityRegister(event: RegisterEntityRenderersEvent) {
        event.registerEntity(ModEntities.TEST_ENTITY, ::GLTFEntityRenderer)
    }
}
