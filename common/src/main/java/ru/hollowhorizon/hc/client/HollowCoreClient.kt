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
import net.minecraft.client.renderer.entity.EntityRenderers
import ru.hollowhorizon.hc.client.render.RenderLoader
import ru.hollowhorizon.hc.client.render.effekseer.EffekseerNatives
import ru.hollowhorizon.hc.client.render.entity.GLTFEntityRenderer
import ru.hollowhorizon.hc.common.registry.ModEntities

object HollowCoreClient {

    init {
        EntityRenderers.register(ModEntities.TEST_ENTITY, ::GLTFEntityRenderer)

        //TODO Register it
//        MOD_BUS.addListener(::onRegisterReloadListener)
//        MOD_BUS.addListener(::onRegisterResourcePacks)
//        MOD_BUS.addListener(::onCreatingRenderers)
//        MOD_BUS.addListener(::onCreatingMenus)
//
//        MOD_BUS.register(ModShaders)
//        FORGE_BUS.addListener(ModShaders::onHollowShadersRegistry)
//
//        FORGE_BUS.register(TickHandler)

        EffekseerNatives.install()
        RenderSystem.recordRenderCall(RenderLoader::onInitialize)
    }
//
//    private fun onRegisterReloadListener(event: RegisterClientReloadListenersEvent) {
//        event.registerReloadListener(EffekAssets)
//        event.registerReloadListener(GltfManager)
//        event.registerReloadListener(PostChain)
//        event.registerReloadListener(ShadersLoader)
//    }

//    private fun onRegisterResourcePacks(event: AddPackFindersEvent) {
//        event.addRepositorySource { adder, creator ->
//            adder.accept(
//                creator.create(
//                    HollowPack.name, HollowPack.name.mcText, true, { HollowPack },
//                    HollowPack.section, Pack.Position.TOP, PackSource.BUILT_IN, HollowPack.isHidden
//                )
//            )
//        }
//    }

//    @OnlyIn(Dist.CLIENT)
//    private fun onCreatingRenderers(event: EntityRenderersEvent.RegisterRenderers) {
//        event.registerEntityRenderer(ModEntities.TEST_ENTITY.get(), ::GLTFEntityRenderer)
//    }

//    private fun onCreatingMenus(event: FMLClientSetupEvent) {
//        event.enqueueWork {
//            MenuScreens.register(ModMenus.HOLLOW_MENU.get(), ::HollowMenuScreen)
//        }
//    }
}