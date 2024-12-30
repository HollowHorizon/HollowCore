package ru.hollowhorizon.hc.client.kool.support

import com.mojang.blaze3d.vertex.DefaultVertexFormat
import de.fabmax.kool.pipeline.Attribute
import de.fabmax.kool.pipeline.GpuType

val MC_POSITION = Attribute("Position", GpuType.FLOAT3)
val MC_COLOR = Attribute("Color", GpuType.FLOAT4)
val MC_UV_0 = Attribute("UV0", GpuType.FLOAT2)
val MC_UV_1 = Attribute("UV1", GpuType.INT2)
val MC_UV_2 = Attribute("UV2", GpuType.INT2)
val MC_NORMAL = Attribute("Normal", GpuType.FLOAT3)
