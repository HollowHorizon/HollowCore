package ru.hollowhorizon.hc.common.objects.entities

import net.minecraft.entity.Entity
import ru.hollowhorizon.hc.client.models.core.bonemf.BoneMFSkeleton
import ru.hollowhorizon.hc.common.objects.entities.animation.AnimationComponent


interface IBTAnimatedEntity<T> where T : Entity, T : IBTAnimatedEntity<T> {
    val animationComponent: AnimationComponent<T>
    val skeleton: BoneMFSkeleton
}