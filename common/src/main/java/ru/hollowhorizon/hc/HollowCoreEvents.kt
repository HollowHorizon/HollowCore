package ru.hollowhorizon.hc

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeSupplier

object HollowCoreEvents {
    lateinit var registerAttributesEvent: (type: EntityType<out LivingEntity>, container: AttributeSupplier) -> Unit
}