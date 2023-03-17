package ru.hollowhorizon.hc.client.render.entity.layers

import net.minecraft.entity.Entity
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.client.render.entity.BTAnimatedEntityRenderer
import ru.hollowhorizon.hc.common.objects.entities.IBTAnimatedEntity

@OnlyIn(Dist.CLIENT)
abstract class BTAnimatedLayerRenderer<T>(val entityRenderer: BTAnimatedEntityRenderer<T>) :
    IBTAnimatedLayerRenderer<T> where T : Entity, T : IBTAnimatedEntity<T> {

}