package ru.hollowhorizon.hc.client.models.internal

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation
import org.joml.Matrix4f
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.*
import ru.hollowhorizon.hc.client.models.internal.manager.GltfManager
import ru.hollowhorizon.hc.client.models.gltf.*
import ru.hollowhorizon.hc.client.utils.areShadersEnabled
import ru.hollowhorizon.hc.client.utils.hasShaders
import ru.hollowhorizon.hc.client.utils.math.MikkTSpaceContext
import ru.hollowhorizon.hc.client.utils.math.MikktspaceTangentGenerator
import ru.hollowhorizon.hc.client.utils.toTexture
import java.nio.FloatBuffer
import java.util.ArrayList

data class Primitive(
    val attributes: Map<String, GltfAccessor>,
    val indices: GltfAccessor? = null,
    val mode: Int,
    val material: Material,
    val morphTargets: List<Map<String, FloatArray>> = ArrayList(),
) {
    val hasSkinning = attributes[GltfMesh.Primitive.ATTRIBUTE_JOINTS_0] != null
            && attributes[GltfMesh.Primitive.ATTRIBUTE_WEIGHTS_0] != null
    private val indexCount = indices?.count ?: 0
    private val positionsCount = (attributes[GltfMesh.Primitive.ATTRIBUTE_POSITION]?.count ?: 0) * 3
    var jointCount = 0
    val morphCommands = ArrayList<(FloatArray) -> Unit>()

    private var vao = -1
    private var skinningVao = -1

    private var vertexBuffer = -1
    private var normalBuffer = -1
    private var tangentBuffer = -1
    private var texCoordsBuffer = -1
    private var midCoordsBuffer = -1
    private var indexBuffer = -1

    private var glTexture = -1
    private var jointBuffer = -1
    private var weightsBuffer = -1
    private var skinVertexBuffer = -1
    private var skinNormalBuffer = -1
    private var jointMatrixBuffer = -1

    fun init() {
        val currentVAO = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING)
        val currentArrayBuffer = GL33.glGetInteger(GL33.GL_ARRAY_BUFFER_BINDING)
        val currentElementArrayBuffer = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING)

        if (hasSkinning) initTransformFeedback()
        initBuffers()

        GL33.glBindVertexArray(currentVAO)
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, currentArrayBuffer)
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)
    }

    private fun initBuffers() {
        val positions = attributes[GltfMesh.Primitive.ATTRIBUTE_POSITION]?.let { Vec3fAccessor(it) }?.list
        val normals = attributes[GltfMesh.Primitive.ATTRIBUTE_NORMAL]?.let { Vec3fAccessor(it) }?.list
        val texCoord0 = attributes[GltfMesh.Primitive.ATTRIBUTE_TEXCOORD_0]?.let { Vec2fAccessor(it) }?.list
        val texCoord1 = attributes[GltfMesh.Primitive.ATTRIBUTE_TEXCOORD_1]?.let { Vec2fAccessor(it) }?.list
        val tangents = attributes[GltfMesh.Primitive.ATTRIBUTE_TANGENT]?.let { Vec4fAccessor(it) }?.list

        vao = GL33.glGenVertexArrays()
        GL33.glBindVertexArray(vao)

        if (skinningVao == -1) {
            if (positions != null) {
                val buffer = BufferUtils.createFloatBuffer(positions.size * 3)

                positions.forEach { buffer.put(it.x).put(it.y).put(it.z) }
                buffer.flip()

                morphCommands += { array ->
                    for (i in 0 until positions.size * 3) {
                        var value = positions[i / 3].toArray()[i % 3]
                        array.forEachIndexed { j, shapeKey ->
                            morphTargets[j][GltfMesh.Primitive.ATTRIBUTE_POSITION]?.let {
                                value += it[i] * shapeKey
                            }
                        }
                        buffer.put(i, value)
                    }

                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vertexBuffer)
                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
                }

                vertexBuffer = GL33.glGenBuffers()
                GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vertexBuffer)
                GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
                GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0)

            }
            if (normals != null) {
                val buffer = BufferUtils.createFloatBuffer(normals.size * 3)
                for (n in normals) buffer.put(n.x()).put(n.y()).put(n.z())
                buffer.flip()

                morphCommands += { array ->
                    for (i in 0 until normals.size * 3) {
                        var value = normals[i / 3].toArray()[i % 3]
                        array.forEachIndexed { j, percent ->
                            morphTargets[j][GltfMesh.Primitive.ATTRIBUTE_NORMAL]?.let {
                                value += it[i] * percent
                            }
                        }
                        buffer.put(i, value)
                    }
                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, normalBuffer)
                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
                }

                normalBuffer = GL33.glGenBuffers()
                GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, normalBuffer)
                GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
                GL33.glVertexAttribPointer(5, 3, GL33.GL_FLOAT, false, 0, 0)

                if (GltfMesh.Primitive.ATTRIBUTE_TANGENT !in attributes && positions != null) {
                    val tangents = BufferUtils.createFloatBuffer(normals.size * 4)

                    MikktspaceTangentGenerator.genTangSpaceDefault(object : MikkTSpaceContext {
                        override fun getNumFaces(): Int {
                            return positionsCount / 9
                        }

                        override fun getNumVerticesOfFace(face: Int): Int {
                            return 3
                        }

                        override fun getPosition(posOut: FloatArray, face: Int, vert: Int) {
                            val index = (face * 3) + vert
                            posOut[0] = positions[index].x
                            posOut[1] = positions[index].y
                            posOut[2] = positions[index].z
                        }

                        override fun getNormal(normOut: FloatArray, face: Int, vert: Int) {
                            val index = (face * 3) + vert
                            normOut[0] = normals[index].x
                            normOut[1] = normals[index].y
                            normOut[2] = normals[index].z
                        }

                        override fun getTexCoord(texOut: FloatArray, face: Int, vert: Int) {
                            val index = (face * 3) + vert
                            texOut[0] = texCoord0?.get(index)?.x ?: 0f
                            texOut[1] = texCoord0?.get(index)?.y ?: 0f
                        }

                        override fun setTSpaceBasic(tangent: FloatArray, sign: Float, face: Int, vert: Int) {
                            tangents
                                .put(tangent[0])
                                .put(tangent[1])
                                .put(tangent[2])
                                .put(-sign)
                        }

                        override fun setTSpace(
                            tangent: FloatArray?,
                            biTangent: FloatArray?,
                            magS: Float,
                            magT: Float,
                            isOrientationPreserving: Boolean,
                            face: Int,
                            vert: Int,
                        ) {
                        }
                    })

                    tangents.flip()
                    tangentBuffer = GL33.glGenBuffers()
                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, tangentBuffer)
                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, tangents, GL33.GL_STATIC_DRAW)
                    GL33.glVertexAttribPointer(8, 4, GL33.GL_FLOAT, false, 0, 0)
                }
            }

            if (tangents != null) {
                val buffer = BufferUtils.createFloatBuffer(tangents.size * 4)
                for (t in tangents) {
                    buffer.put(t.x()).put(t.y()).put(t.z()).put(1f)
                }
                buffer.flip()

                morphCommands += { array ->
                    for (i in 0 until tangents.size * 3) {
                        var value = tangents[i / 3].toArray()[i % 3]
                        array.forEachIndexed { j, percent ->
                            morphTargets[j][GltfMesh.Primitive.ATTRIBUTE_TANGENT]?.let {
                                value += it[i] * percent
                            }
                        }
                        buffer.put(i, value)
                    }
                    GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, tangentBuffer)
                    GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
                }

                tangentBuffer = GL33.glGenBuffers()
                GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, tangentBuffer)
                GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
                GL33.glVertexAttribPointer(8, 4, GL33.GL_FLOAT, false, 0, 0)
            }
        } else {
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vertexBuffer)
            GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 0, 0)

            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, normalBuffer)
            GL33.glVertexAttribPointer(5, 3, GL33.GL_FLOAT, false, 0, 0)
        }

        if (texCoord0 != null) {
            val buffer = BufferUtils.createFloatBuffer(texCoord0.size * 2)
            for (t in texCoord0) buffer.put(t.x).put(t.y)
            buffer.flip()

            texCoordsBuffer = GL33.glGenBuffers()
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, texCoordsBuffer)
            GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
            GL33.glVertexAttribPointer(2, 2, GL33.GL_FLOAT, false, 0, 0)
            GL33.glVertexAttribPointer(7, 2, GL33.GL_FLOAT, false, 0, 0)
        }

        if (texCoord1 != null) {
            val buffer = BufferUtils.createFloatBuffer(texCoord1.size * 2)
            for (t in texCoord1) buffer.put(t.x).put(t.y)
            buffer.flip()

            midCoordsBuffer = GL33.glGenBuffers()
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, midCoordsBuffer)
            GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
            GL33.glVertexAttribPointer(7, 2, GL33.GL_FLOAT, false, 0, 0)
        }

        GL20.glVertexAttribPointer(1, 4, GL33.GL_FLOAT, false, 0, 0)

        if (indices != null) {
            val values = IntAccessor(indices).list
            val buffer = BufferUtils.createIntBuffer(values.size)
            for (n in values) buffer.put(n)
            buffer.flip()

            indexBuffer = GL33.glGenBuffers()
            GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, indexBuffer)
            GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
        }
    }

    private fun initTransformFeedback() {
        val weights = attributes[GltfMesh.Primitive.ATTRIBUTE_WEIGHTS_0]?.let { Vec4fAccessor(it) }?.list ?: return
        val joints = attributes[GltfMesh.Primitive.ATTRIBUTE_JOINTS_0]?.let { Vec4iAccessor(it) }?.list ?: return
        val positions = attributes[GltfMesh.Primitive.ATTRIBUTE_POSITION]?.let { Vec3fAccessor(it) }?.list
        val normals = attributes[GltfMesh.Primitive.ATTRIBUTE_NORMAL]?.let { Vec3fAccessor(it) }?.list

        skinningVao = GL30.glGenVertexArrays()
        GL30.glBindVertexArray(skinningVao)

        var posSize = -1L
        var norSize = -1L


        val jointBuffer = BufferUtils.createIntBuffer(joints.size * 4)
        for (n in joints) jointBuffer.put(n.x).put(n.y).put(n.z).put(n.w)
        jointBuffer.flip()

        this.jointBuffer = GL33.glGenBuffers()
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.jointBuffer)
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, jointBuffer, GL33.GL_STATIC_DRAW)
        GL33.glVertexAttribPointer(0, 4, GL33.GL_INT, false, 0, 0)

        val weightsBuffer = BufferUtils.createFloatBuffer(weights.size * 4)
        for (n in weights) weightsBuffer.put(n.x()).put(n.y()).put(n.z()).put(n.w())
        weightsBuffer.flip()

        this.weightsBuffer = GL33.glGenBuffers()
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, this.weightsBuffer)
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, weightsBuffer, GL33.GL_STATIC_DRAW)
        GL33.glVertexAttribPointer(1, 4, GL33.GL_FLOAT, false, 0, 0)

        if (positions != null) {
            posSize = positions.size * 12L //bytes size
            val buffer = BufferUtils.createFloatBuffer(positions.size * 3)
            for (n in positions) buffer.put(n.x()).put(n.y()).put(n.z())
            buffer.flip()

            skinVertexBuffer = GL33.glGenBuffers()
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, skinVertexBuffer)
            GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
            GL33.glVertexAttribPointer(2, 3, GL33.GL_FLOAT, false, 0, 0)

        }
        if (normals != null) {
            norSize = normals.size * 12L //bytes size
            val buffer = BufferUtils.createFloatBuffer(normals.size * 3)
            for (n in normals) buffer.put(n.x()).put(n.y()).put(n.z())
            buffer.flip()

            skinNormalBuffer = GL33.glGenBuffers()
            GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, skinNormalBuffer)
            GL33.glBufferData(GL33.GL_ARRAY_BUFFER, buffer, GL33.GL_STATIC_DRAW)
            GL33.glVertexAttribPointer(3, 3, GL33.GL_FLOAT, false, 0, 0)

        }

        vertexBuffer = GL33.glGenBuffers()
        GL33.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, vertexBuffer)
        GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, posSize, GL33.GL_STATIC_DRAW)

        normalBuffer = GL33.glGenBuffers()
        GL33.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, normalBuffer)
        GL15.glBufferData(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, norSize, GL33.GL_STATIC_DRAW)

        jointMatrixBuffer = GL15.glGenBuffers()
        GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, jointMatrixBuffer)
        GL15.glBufferData(GL31.GL_TEXTURE_BUFFER, jointCount * 64L, GL15.GL_STATIC_DRAW)
        glTexture = GL11.glGenTextures()
        GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, glTexture)
        GL31.glTexBuffer(GL31.GL_TEXTURE_BUFFER, GL30.GL_RGBA32F, jointMatrixBuffer)
        GL11.glBindTexture(GL31.GL_TEXTURE_BUFFER, 0)

        GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0)
        GL15.glBindBuffer(GL31.GL_TEXTURE_BUFFER, 0)
    }

    fun render(
        stack: PoseStack,
        node: Node,
        consumer: (ResourceLocation) -> Int,
    ) {
        if (morphTargets.isNotEmpty()) updateMorphTargets(node)

        val shader = AnimatedModel.SHADER
        //Всякие настройки смешивания, материалы и т.п.
        val texture = consumer(material.texture)

        if (!node.isAllHovered()) GL33.glVertexAttrib4f(
            1, material.color.x(), material.color.y(), material.color.z(), material.color.w()
        )
        else GL33.glVertexAttrib4f(1, 0f, 0.45f, 1f, 1f)

        var normal = 0
        var specular = 0

        if (areShadersEnabled) {
            //т.к. Iris использует отличные от Optifine id текстур стоит взять их из самого шейдера
            GL33.glGetUniformLocation(shader.id, "normals").takeIf { it != -1 }?.let {
                GL33.glActiveTexture(COLOR_MAP_INDEX + GL33.glGetUniformi(shader.id, it))
                normal = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                GL33.glBindTexture(GL33.GL_TEXTURE_2D, material.normalTexture.toTexture().id)
            }
            GL33.glGetUniformLocation(shader.id, "specular").takeIf { it != -1 }?.let {
                GL33.glActiveTexture(COLOR_MAP_INDEX + GL33.glGetUniformi(shader.id, it))
                specular = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)
                GL33.glBindTexture(GL33.GL_TEXTURE_2D, material.specularTexture.toTexture().id)
            }
        }

        GL13.glActiveTexture(COLOR_MAP_INDEX + 2)
        GL13.glBindTexture(GL33.GL_TEXTURE_2D, GltfManager.lightTexture.id)
        GL13.glActiveTexture(COLOR_MAP_INDEX)
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, texture)

        if (material.doubleSided) RenderSystem.disableCull()
        //Подключение VAO и IBO
        GL33.glBindVertexArray(vao)
        if (indexBuffer != -1) GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, indexBuffer)

        GL33.glEnableVertexAttribArray(0) // Вершины (или цвет)
        if (texCoordsBuffer != -1) GL33.glEnableVertexAttribArray(2) // Текстурные координаты
        GL33.glEnableVertexAttribArray(5) // Нормали
        if (tangentBuffer != -1) GL33.glEnableVertexAttribArray(8) //Тангенты
        if (hasShaders) GL20.glEnableVertexAttribArray(7) //координаты для глубины (pbr)

        //? if >=1.20.1 {
        val modelView = Matrix4f(RenderSystem.getModelViewMatrix()).mul(stack.last().pose())
        shader.MODEL_VIEW_MATRIX?.set(modelView)
        //?} else {
        /*val modelView = Matrix4f(RenderSystem.getModelViewMatrix().fromMc()).mul(stack.last().pose().fromMc())
        shader.MODEL_VIEW_MATRIX?.set(modelView.toMc())
        *///?}

        shader.MODEL_VIEW_MATRIX?.upload()

        //Нормали
        shader.getUniform("NormalMat")?.let {
            it.set(stack.last().normal())
            it.upload()
        }

        //Отрисовка
        if (indexBuffer != -1) GL33.glDrawElements(GL33.GL_TRIANGLES, indexCount, GL33.GL_UNSIGNED_INT, 0L)
        else GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, positionsCount)

        if (material.doubleSided) RenderSystem.enableCull()

        if (hasShaders) {
            //т.к. Iris использует отличные от Optifine id текстур стоит взять их из самого шейдера
            GL33.glGetUniformLocation(shader.id, "normals").takeIf { it != -1 }?.let {
                GL33.glActiveTexture(COLOR_MAP_INDEX + GL33.glGetUniformi(shader.id, it))
                GL33.glBindTexture(GL33.GL_TEXTURE_2D, normal)
            }
            GL33.glGetUniformLocation(shader.id, "specular").takeIf { it != -1 }?.let {
                GL33.glActiveTexture(COLOR_MAP_INDEX + GL33.glGetUniformi(shader.id, it))
                GL33.glBindTexture(GL33.GL_TEXTURE_2D, specular)
            }
        }

        GL13.glActiveTexture(COLOR_MAP_INDEX)
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0)
        GL33.glCullFace(GL33.GL_BACK)

        //Отключение параметров выше
        GL33.glDisableVertexAttribArray(0)
        if (texCoordsBuffer != -1) GL33.glDisableVertexAttribArray(2)
        GL33.glDisableVertexAttribArray(5)
        if (tangentBuffer != -1) GL33.glDisableVertexAttribArray(8)
        if (hasShaders) GL20.glDisableVertexAttribArray(7)
    }

    private fun updateMorphTargets(node: Node) {
        val weights = node.transform.weights.toFloatArray()

        morphCommands.forEach { it(weights) }
    }

    fun transformSkinning(node: Node, stack: PoseStack) {
        val texBind = GL33.glGetInteger(GL33.GL_ACTIVE_TEXTURE)

        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL33.glBindBuffer(GL33.GL_TEXTURE_BUFFER, jointMatrixBuffer)
        GL33.glBufferSubData(GL33.GL_TEXTURE_BUFFER, 0, computeMatrices(node, stack))

        GL33.glBindTexture(GL33.GL_TEXTURE_BUFFER, glTexture)

        GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0, vertexBuffer)
        GL30.glBindBufferBase(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 1, normalBuffer)

        GL30.glBeginTransformFeedback(GL11.GL_POINTS)
        GL30.glBindVertexArray(skinningVao)
        for (i in 0..3) GL33.glEnableVertexAttribArray(i)
        GL11.glDrawArrays(GL11.GL_POINTS, 0, positionsCount)
        for (i in 0..3) GL33.glDisableVertexAttribArray(i)
        GL30.glBindVertexArray(0)
        GL30.glEndTransformFeedback()
        GL13.glActiveTexture(texBind)
    }

    private fun computeMatrices(node: Node, stack: PoseStack): FloatBuffer {
        val matrices = node.skin!!.finalMatrices(node)

        val buffer = BufferUtils.createFloatBuffer(matrices.size * 16)
        for (m in matrices) {
            // Угадайте сколько часов у меня ушло, чтобы угадать, в каком порядке я должен передавать эти значения :(
            buffer.put(m.m00())
            buffer.put(m.m01())
            buffer.put(m.m02())
            buffer.put(m.m03())
            buffer.put(m.m10())
            buffer.put(m.m11())
            buffer.put(m.m12())
            buffer.put(m.m13())
            buffer.put(m.m20())
            buffer.put(m.m21())
            buffer.put(m.m22())
            buffer.put(m.m23())
            buffer.put(m.m30())
            buffer.put(m.m31())
            buffer.put(m.m32())
            buffer.put(m.m33())
        }
        buffer.flip()
        return buffer
    }

    fun destroy() {
        GL30.glDeleteVertexArrays(vao)
        GL30.glDeleteVertexArrays(skinningVao)

        GL30.glDeleteBuffers(indexBuffer)
        GL30.glDeleteBuffers(vertexBuffer)
        GL30.glDeleteBuffers(texCoordsBuffer)
        GL30.glDeleteBuffers(normalBuffer)
        GL30.glDeleteBuffers(midCoordsBuffer)

        GL30.glDeleteBuffers(skinVertexBuffer)
        GL30.glDeleteBuffers(skinNormalBuffer)
    }
}