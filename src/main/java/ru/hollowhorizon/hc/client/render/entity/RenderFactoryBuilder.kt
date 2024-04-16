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

package ru.hollowhorizon.hc.client.render.entity

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraftforge.client.event.EntityRenderersEvent
import net.minecraftforge.common.MinecraftForge
import ru.hollowhorizon.hc.HollowCore

object RenderFactoryBuilder {
    @JvmStatic
    fun <T : Entity> buildEntity(entityType: () -> EntityType<T>, rendererClass: Class<EntityRenderer<T>>) {
        MinecraftForge.EVENT_BUS.addListener<EntityRenderersEvent.RegisterRenderers> { event ->
            event.registerEntityRenderer(entityType()) { manager ->
                try {
                    rendererClass.getConstructor(EntityRendererProvider.Context::class.java).newInstance(manager)
                } catch (e: Exception) {
                    HollowCore.LOGGER.error(
                        "Error, when creating renderer ${rendererClass.name} for entity ${entityType().descriptionId}: ",
                        e
                    )
                    throw e
                }
            }
        }
    }

    fun <T : BlockEntity> buildTileEntity(
        tileEntityType: () -> BlockEntityType<T>,
        rendererClass: Class<BlockEntityRenderer<T>>,
    ) {
        MinecraftForge.EVENT_BUS.addListener<EntityRenderersEvent.RegisterRenderers> { event ->
            event.registerBlockEntityRenderer(tileEntityType()) { manager ->
                try {
                    rendererClass.getConstructor(BlockEntityRendererProvider.Context::class.java).newInstance(manager)
                } catch (e: Exception) {
                    HollowCore.LOGGER.error(
                        "Error, when creating renderer ${rendererClass.name} for block entity ${tileEntityType}: ",
                        e
                    )
                    throw e
                }
            }
        }

    }

}