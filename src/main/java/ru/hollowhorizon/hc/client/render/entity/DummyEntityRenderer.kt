package ru.hollowhorizon.hc.client.render.entity

import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererManager
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.entity.Entity
import net.minecraft.util.ResourceLocation

class DummyEntityRenderer<T : Entity>(dispatcher: EntityRendererManager) : EntityRenderer<T>(dispatcher) {
    override fun getTextureLocation(entity: T): ResourceLocation = TextureManager.INTENTIONAL_MISSING_TEXTURE
}