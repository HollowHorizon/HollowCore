package ru.hollowhorizon.hc.common.events.entity

import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import ru.hollowhorizon.hc.common.events.Cancelable
import ru.hollowhorizon.hc.common.events.Event

open class EntityHurtEvent(val entity: Entity, val source: DamageSource, var amount: Float): Event, Cancelable {
    override var isCanceled: Boolean = false
}