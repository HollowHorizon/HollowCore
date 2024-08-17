package ru.hollowhorizon.hc.common.events.registry

import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import ru.hollowhorizon.hc.common.events.Event

class RegisterEntityRenderersEvent(private val consumer: (EntityType<out Entity>, EntityRendererProvider<Entity>) -> Unit) :
    Event {
    fun <T: Entity> registerEntity(entity: EntityType<out T>, provider: EntityRendererProvider<T>) {
        consumer(entity, provider as EntityRendererProvider<Entity>)
    }
}