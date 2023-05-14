package ru.hollowhorizon.hc.client.render.entity

import net.minecraft.client.gui.IHasContainer
import net.minecraft.client.gui.ScreenManager
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.tileentity.TileEntityRenderer
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.Container
import net.minecraft.inventory.container.ContainerType
import net.minecraft.tileentity.TileEntity
import net.minecraft.tileentity.TileEntityType
import net.minecraft.util.text.ITextComponent
import net.minecraftforge.fml.client.registry.ClientRegistry
import net.minecraftforge.fml.client.registry.RenderingRegistry

object RenderFactoryBuilder {
    fun <T : Entity> buildEntity(entityType: EntityType<T>, rendererClass: Class<EntityRenderer<T>>) {
        RenderingRegistry.registerEntityRenderingHandler(entityType) { manager ->
            try {
                rendererClass.getConstructor(EntityRendererManager::class.java).newInstance(manager)
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    fun <T : TileEntity> buildTileEntity(
        tileEntityType: TileEntityType<T>,
        rendererClass: Class<TileEntityRenderer<T>>,
    ) {
        ClientRegistry.bindTileEntityRenderer(tileEntityType) { dispatcher ->
            rendererClass.getConstructor(TileEntityRendererDispatcher::class.java).newInstance(dispatcher)
        }
    }

    fun <C : Container, T> buildContainerScreen(
        containerClass: ContainerType<C>,
        screenClass: Class<T>,
    ) where T : IHasContainer<C>, T : Screen {
        ScreenManager.register(containerClass) { container, inventory, text ->
            return@register screenClass.getConstructor(
                Container::class.java,
                Inventory::class.java,
                ITextComponent::class.java
            ).newInstance(container, inventory, text)
        }
    }
}