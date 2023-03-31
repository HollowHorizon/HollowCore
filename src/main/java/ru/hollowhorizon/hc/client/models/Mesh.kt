package ru.hollowhorizon.hc.client.models

import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.renderer.vertex.VertexBuffer
import net.minecraft.client.renderer.vertex.VertexFormat
import net.minecraft.util.math.vector.Vector3f
import java.nio.ByteBuffer
import java.nio.FloatBuffer
import java.nio.IntBuffer

class Mesh {
    private val meshBuffer: VertexBuffer
    private val vertexBuffer: FloatBuffer? = null
    private val indexBuffer: ByteBuffer? = null
    private var vertices: List<Vertex>

    // private List<Integer> indices;
    private var indices: IntBuffer
    private var textures: List<Texture>
    private val rigged = false
    private val vbo = 0
    var vAO = 0
    var eBO = 0

    constructor(vertices: List<Vertex>, indices: IntBuffer, textures: List<Texture>) {
        this.vertices = vertices
        this.indices = indices
        this.textures = textures
        meshBuffer = VertexBuffer(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL)
        // setupMesh();
    }

    constructor(
        vertices: List<Vertex>,
        indices: IntBuffer,
        textures: List<Texture>,
        format: VertexFormat?,
        rigged: Boolean,
    ) {
        this.vertices = vertices
        this.indices = indices
        this.textures = textures
        meshBuffer = VertexBuffer(format)
        // setupMesh();
    }

    val buffer: VertexBuffer
        get() = meshBuffer

    fun getTextures(): List<Texture> {
        return textures
    }

    fun getVertices(): List<Vertex> {
        return vertices
    }

    fun getIndices(): IntBuffer {
        return indices
    }

    val verticesAsVector: List<Any>
        get() {
            val toReturn: MutableList<Vector3f> = ArrayList()
            for (vertex in getVertices()) {
                toReturn.add(Vector3f(vertex.position.x.toFloat(), vertex.position.y.toFloat(), vertex.position.z.toFloat()))
            }
            return toReturn
        }
}