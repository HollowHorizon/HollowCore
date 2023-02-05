package ru.hollowhorizon.hc.client.gltf

import net.minecraft.util.ResourceLocation
import ru.hollowhorizon.hc.client.gltf.animation.GLTFAnimationManager

interface IAnimated {
    fun getModel(): ResourceLocation
    fun getManager(): GLTFAnimationManager

    fun setManager(manager: GLTFAnimationManager)
}