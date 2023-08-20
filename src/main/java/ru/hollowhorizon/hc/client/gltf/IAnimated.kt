package ru.hollowhorizon.hc.client.gltf

import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.gltf.animations.manager.IModelManager

interface IAnimated {
    val model: ResourceLocation
    val manager: IModelManager

}