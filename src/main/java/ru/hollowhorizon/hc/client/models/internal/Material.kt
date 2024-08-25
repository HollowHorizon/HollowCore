package ru.hollowhorizon.hc.client.models.internal

import net.minecraft.resources.ResourceLocation
import org.joml.Vector4f
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.rl

data class Material(
    var color: Vector4f = Vector4f(1f, 1f, 1f, 1f),
    var texture: ResourceLocation = "${HollowCore.MODID}:default_color_map".rl,
    var normalTexture: ResourceLocation = "${HollowCore.MODID}:default_normal_map".rl,
    var specularTexture: ResourceLocation = "${HollowCore.MODID}:default_specular_map".rl,
    var doubleSided: Boolean = false,
)