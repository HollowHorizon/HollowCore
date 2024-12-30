package ru.hollowhorizon.hc.client.kool.support

import com.mojang.blaze3d.vertex.*
import de.fabmax.kool.scene.geometry.IndexedVertexList
import de.fabmax.kool.scene.geometry.VertexView
import net.minecraft.client.renderer.ChunkBufferBuilderPack
import net.minecraft.client.renderer.RenderType
import ru.hollowhorizon.hc.client.kool.currentChunkMesh
import ru.hollowhorizon.hc.client.kool.gl.lightMapUV

class KoolBufferBuilder : BufferBuilder(1), BufferVertexConsumer {
    private var position: VertexView.() -> Unit = {}
    private var normal: VertexView.() -> Unit = {}
    private var color: VertexView.() -> Unit = {
        color.set(defaultR / 255f, defaultG / 255f, defaultB / 255f, defaultA / 255f)
    }
    private var uv: VertexView.() -> Unit = {}
    private var uv2: VertexView.() -> Unit = {}

    private val vertexBuilder = mutableListOf<IndexedVertexList.() -> Unit>()

    private val currentElement: VertexFormatElement? = null

    override fun begin(mode: VertexFormat.Mode, format: VertexFormat) {}

    override fun endVertex() {
        synchronized(vertexBuilder) {
            val addPos = position
            val addNormal = normal
            val addColor = color
            val addUV = uv
            val addUV2 = uv2
            vertexBuilder.add {
                addVertex {
                    addPos() // 3*4f bytes
                    addNormal() // 3*4f bytes
                    addColor() // 4*4f bytes
                    addUV() // 2*4f bytes
                    addUV2() // 2*4f bytes
                    // 56 bytes per vertex (game itself: 31 bytes)
                }
                if (numVertices % 4 == 0) {
                    val pos = numVertices
                    addTriIndices(pos, pos + 1, pos + 2)
                    addTriIndices(pos, pos + 2, pos + 3)
                }
            }
            position = {}
            normal = {}
            color = {}
            uv = {}
            uv2 = {}
        }
    }

    override fun currentElement() = currentElement ?: error("BufferBuilder not started")
    override fun nextElement() {}

    override fun putByte(index: Int, byteValue: Byte) = error("Not used!")
    override fun putShort(index: Int, shortValue: Short) = error("Not used!")
    override fun putFloat(index: Int, floatValue: Float) = error("Not used!")

    override fun vertex(x: Double, y: Double, z: Double): VertexConsumer {
        position = { position.set(x.toFloat(), y.toFloat(), z.toFloat()) }
        return this
    }

    override fun normal(x: Float, y: Float, z: Float): VertexConsumer {
        normal = { normal.set(x, y, z) }
        return this
    }

    override fun color(red: Int, green: Int, blue: Int, alpha: Int): VertexConsumer {
        if (!defaultColorSet) color = { color.set(red.toFloat(), green.toFloat(), blue.toFloat(), alpha.toFloat()) }
        return this
    }

    override fun uv2(u: Int, v: Int): VertexConsumer {
        uv2 = { lightMapUV.set(u, v) }
        return this
    }

    override fun uv(u: Float, v: Float): VertexConsumer {
        uv = { texCoord.set(u, v) }
        return this
    }

    override fun overlayCoords(u: Int, v: Int): VertexConsumer {
        return this
    }

    override fun vertex(
        x: Float,
        y: Float,
        z: Float,
        red: Float,
        green: Float,
        blue: Float,
        alpha: Float,
        texU: Float,
        texV: Float,
        overlayUV: Int,
        lightmapUV: Int,
        normalX: Float,
        normalY: Float,
        normalZ: Float,
    ) {
        vertex(x.toDouble(), y.toDouble(), z.toDouble())
        color(red, green, blue, alpha)
        uv(texU, texV)
        uv2(lightmapUV)
        normal(normalX, normalY, normalZ)
        endVertex()
    }

    override fun endOrDiscardIfEmpty(): RenderedBuffer? {
        synchronized(vertexBuilder) {
            currentChunkMesh?.let { mesh ->
                mesh.geometry.batchUpdate(true) {
                    vertexBuilder.forEach { it() }
                }
            }
            vertexBuilder.clear()
        }
        return null
    }
}

class KoolChunkBufferBuilderPack : ChunkBufferBuilderPack() {
    val builder = KoolBufferBuilder()

    override fun builder(renderType: RenderType): BufferBuilder {
        when (renderType) {
            RenderType.cutout(), RenderType.cutoutMipped() -> currentChunkMesh?.drawGroupId = 1
            RenderType.translucent() -> currentChunkMesh?.drawGroupId = 2
        }

        return builder
    }
}