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
    fun <T : Entity> buildEntity(entityType: EntityType<T>, rendererClass: Class<EntityRenderer<T>>) {
        MinecraftForge.EVENT_BUS.addListener<EntityRenderersEvent.RegisterRenderers> { event ->
            event.registerEntityRenderer(entityType) { manager ->
                try {
                    rendererClass.getConstructor(EntityRendererProvider.Context::class.java).newInstance(manager)
                } catch (e: Exception) {
                    HollowCore.LOGGER.error(
                        "Error, when creating renderer ${rendererClass.name} for entity ${entityType.descriptionId}: ",
                        e
                    )
                    throw e
                }
            }
        }
    }

    fun <T : BlockEntity> buildTileEntity(
        tileEntityType: BlockEntityType<T>,
        rendererClass: Class<BlockEntityRenderer<T>>,
    ) {
        MinecraftForge.EVENT_BUS.addListener<EntityRenderersEvent.RegisterRenderers> { event ->
            event.registerBlockEntityRenderer(tileEntityType) { manager ->
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