package ru.hollowhorizon.hc.client.kool.example

import de.fabmax.kool.modules.ksl.KslShader
import de.fabmax.kool.modules.ksl.lang.*
import kotlin.reflect.KProperty


val GltfEntity = KslShader("GltfEntity") {
    val uSampler0 = texture2d("Sampler0")
    val uSampler1 = texture2d("Sampler1")
    val uSampler2 = texture2d("Sampler2")

    val uColorModulator = uniformFloat4("ColorModulator")
    val uFogStart = uniformFloat1("FogStart")
    val uFogEnd = uniformFloat1("FogEnd")
    val uFogColor = uniformFloat4("FogColor")
    val uModelViewMat = uniformMat4("ModelViewMat")
    val uNormalMat = uniformMat3("NormalMat")
    val uProjMat = uniformMat4("ProjMat")
    val uIViewRotMat = uniformMat3("IViewRotMat")
    val uFogShape = uniformInt1("FogShape")
    val uLight0Direction = uniformFloat3("Light0_Direction")
    val uLight1Direction = uniformFloat3("Light1_Direction")

    val interVertexDistance = interStageFloat1()
    val interVertexColor = interStageFloat4()
    val interLightMapColor = interStageFloat4()
    val interOverlayColor = interStageFloat4()
    val interTexCoord0 = interStageFloat2()

    vertexStage {
        val inPosition = vertexAttribFloat3("Position")
        val inColor = vertexAttribFloat4("Color")
        val inUV0 = vertexAttribFloat2("UV0")
        val inUV1 = vertexAttribFloat2("UV1")
        val inUV2 = vertexAttribFloat2("UV2")
        val inNormal = vertexAttribFloat3("Normal")

        main {
            val position = float4Var(uProjMat * uModelViewMat * float4Value(inPosition, 1f.const))
            outPosition set position

            val worldPosition = float3Var(uIViewRotMat * inPosition)
            interVertexDistance.input set fogDistance(uModelViewMat, worldPosition, uFogShape)

            val fixNormal = float3Var(normalize(uNormalMat * inNormal))

            interVertexColor.input set minecraftMixLight(
                uLight0Direction, uLight1Direction, fixNormal, inColor
            )

            interLightMapColor.input set texelFetch(uSampler2, inUV2 / float2Value(16f, 16f), 0.const)
            interOverlayColor.input set texelFetch(uSampler1, inUV1, 0.const)

            interTexCoord0.input set inUV0
        }
    }

    fragmentStage {
        main {
            val color = float4Var(sampleTexture(uSampler0, interTexCoord0.output))

            `if`(color.a lt 0.1f.const) {
                discard()
            }

            color *= interVertexColor.output * uColorModulator
            color.rgb set mix(interOverlayColor.output.rgb, color.rgb, interOverlayColor.output.a)
            color *= interLightMapColor.output
            color set linearFog(color, interVertexDistance.output, uFogStart, uFogEnd, uFogColor)

            colorOutput(color)
        }
    }
}

fun KslScopeBuilder.linearFog(
    inColor: KslExprFloat4,
    vertexDistance: KslExprFloat1,
    fogStart: KslExprFloat1,
    fogEnd: KslExprFloat1,
    fogColor: KslExprFloat4,
): KslVectorExpression<KslFloat4, KslFloat1> {
    val func = parentStage.getOrCreateFunction("linearFog") { LinearFog(this) }
    return func(inColor, vertexDistance, fogStart, fogEnd, fogColor)
}

private class LinearFog(parentScope: KslScopeBuilder) :
    KslFunction<KslFloat4>("linearFog", KslFloat4, parentScope.parentStage) {
    init {
        val inColor = paramFloat4("inColor")
        val vertexDistance = paramFloat1("vertexDistance")
        val fogStart = paramFloat1("fogStart")
        val fogEnd = paramFloat1("fogEnd")
        val fogColor = paramFloat4("fogColor")

        body.apply {
            `if`(vertexDistance le fogStart) {
                `return`(inColor)
            }.`else` {
                val fogValue = float1Var()
                `if`(vertexDistance lt fogEnd) {
                    fogValue set smoothStep(fogStart, fogEnd, vertexDistance)
                }.`else` {
                    fogValue set 1f.const
                }

                float4Value(mix(inColor.rgb, fogColor.rgb, fogValue * fogColor.a), inColor.a)
            }
        }
    }
}

private class FogDistance(parentScope: KslScopeBuilder) :
    KslFunction<KslFloat1>("fogDistance", KslFloat1, parentScope.parentStage) {
    init {
        val modelViewMat = paramMat4("modelViewMat")
        val pos = paramFloat3("pos")
        val shape = paramInt1("shape")

        body.apply {
            `if`(shape eq 0.const) {
                // shape == 0: Calculate full distance
                `return`(length((modelViewMat * float4Value(pos, 1f.const)).xyz))
            }.`else` {
                // shape != 0: Separate XZ and Y distances
                val distXZ = float1Var(length((modelViewMat * float4Value(pos.x, 0f.const, pos.z, 1f.const)).xyz))
                val distY = float1Var(length((modelViewMat * float4Value(0f.const, pos.y, 0f.const, 1f.const)).xyz))
                `return`(max(distXZ, distY))
            }
        }
    }
}

val MINECRAFT_LIGHT_POWER = KslValueFloat1(0.6f)
val MINECRAFT_AMBIENT_LIGHT = KslValueFloat1(0.4f)

private class MinecraftMixLight(parentScope: KslScopeBuilder) :
    KslFunction<KslFloat4>("minecraftMixLight", KslFloat4, parentScope.parentStage) {
    init {
        val lightDir0 = paramFloat3("lightDir0")
        val lightDir1 = paramFloat3("lightDir1")
        val normal = paramFloat3("normal")
        val color = paramFloat4("color")

        body.apply {
            val dir0 = float3Var(normalize(lightDir0))
            val dir1 = float3Var(normalize(lightDir1))

            val light0 = float1Var(max(0f.const, dot(dir0, normal)))
            val light1 = float1Var(max(0f.const, dot(dir1, normal)))

            val lightAccum =
                float1Var(min(1f.const, (light0 + light1) * MINECRAFT_LIGHT_POWER + MINECRAFT_AMBIENT_LIGHT))

            `return`(float4Value(color.rgb * lightAccum, color.a))
        }
    }
}

fun KslScopeBuilder.fogDistance(
    modelViewMat: KslExprMat4,
    pos: KslExprFloat3,
    shape: KslExprInt1,
): KslScalarExpression<KslFloat1> {
    val func = parentStage.getOrCreateFunction("fogDistance") { FogDistance(this) }
    return func(modelViewMat, pos, shape)
}

fun KslScopeBuilder.minecraftMixLight(
    lightDir0: KslExprFloat3,
    lightDir1: KslExprFloat3,
    normal: KslExprFloat3,
    color: KslExprFloat4,
): KslVectorExpression<KslFloat4, KslFloat1> {
    val func = parentStage.getOrCreateFunction("minecraftMixLight") { MinecraftMixLight(this) }
    return func(lightDir0, lightDir1, normal, color)
}

operator fun <T : KslType> KslInterStageVar<T>.getValue(thisRef: Any?, property: KProperty<*>): KslValue<T> {
    return this.output
}