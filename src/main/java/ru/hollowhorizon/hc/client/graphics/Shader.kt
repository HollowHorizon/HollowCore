package ru.hollowhorizon.hc.client.graphics

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Matrix3f
import com.mojang.math.Matrix4f
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL33
import org.lwjgl.opengl.GL40
import org.lwjgl.opengl.GL43
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.toIS
import java.nio.FloatBuffer
import java.nio.IntBuffer


fun main() {
    val shader = ShaderProgram {
        +Shader(Shader.Type.VERTEX, "hc:shaders/vertex_vao.glsl".rl)
        +Shader(Shader.Type.FRAGMENT, "hc:shaders/fragment_vao.glsl".rl)

        attributes {
            indices(
                0, 1, 2, 3, 1, 2,
                4, 5, 6, 7, 5, 6,
                0, 4, 1, 5, 4, 1,
                2, 6, 3, 7, 6, 3,
                0, 2, 4, 6, 2, 4,
                1, 3, 5, 7, 3, 5
            )

            "position"(
                AttributeContext.Type.VEC3,
                0f, 0f, 0f,
                1f, 0f, 0f,
                0f, 0f, 1f,
                1f, 0f, 1f,
                0f, 1f, 0f,
                1f, 1f, 0f,
                0f, 1f, 1f,
                1f, 1f, 1f,
            )
            "normal"(
                AttributeContext.Type.VEC3,
                0f, 0f, 0f,
                1f, 0f, 0f,
                0f, 0f, 1f,
                1f, 0f, 1f,
                0f, 1f, 0f,
                1f, 1f, 0f,
                0f, 1f, 1f,
                1f, 1f, 1f,
            )
            "texcoords"(
                AttributeContext.Type.VEC2,
                0f, 0f,
                1f, 0f,
                0f, 1f,
                1f, 1f,
                0f, 0f,
                1f, 0f,
                0f, 1f,
                1f, 1f
            )
        }

        uniforms("matrixStack", "texture")
    }

    val stack = PoseStack()
    shader.use {
        "matrixStack"(stack.last().pose())
        "texture"(RenderSystem.getShaderTexture(0))
    }
}

class ShaderProgram(onInit: ShaderProgram.() -> Unit) : ResourceManagerReloadListener {
    private val shaders = mutableListOf<Shader>()
    val attributes = mutableListOf<String>()
    private var programId: Int = -1
    val uniforms = hashMapOf<String, Int>()
    val uniformContext = UniformContext(this)
    val atributeContext = AttributeContext(this)

    init {
        onInit()
        compile()
    }

    fun attributes(onCreate: AttributeContext.() -> Unit) {
        atributeContext.vao.bindVAO()
        atributeContext.onCreate()
        atributeContext.vao.unbindVAO()
    }

    fun uniforms(vararg uniforms: String) {
        this.uniforms.clear()
        this.uniforms.putAll(uniforms.associateWith { GL30.glGetUniformLocation(programId, it) })
    }

    fun bind() = GL30.glUseProgram(programId)
    fun unbind() = GL30.glUseProgram(0)

    fun use(loadUniforms: UniformContext.() -> Unit = {}) {
        bind()
        this.uniformContext.loadUniforms()
        atributeContext.vao.bind()
        GL30.glDrawElements(GL30.GL_TRIANGLES, atributeContext.vao.indexCount, GL30.GL_UNSIGNED_INT, 0)
        unbind()
    }

    operator fun Shader.unaryPlus() {
        shaders += this
    }

    fun compile() {
        programId = GL30.glCreateProgram()
        shaders.forEach { shader ->
            val shaderId = GL30.glCreateShader(shader.type.id)
            GL30.glShaderSource(shaderId, shader.source.toIS().bufferedReader().readText().replace("\t", "    "))
            GL30.glCompileShader(shaderId)
            if (GL30.glGetShaderi(shaderId, GL30.GL_COMPILE_STATUS) == 0) {
                throw IllegalStateException(
                    "Error compiling Shader(${shader.source}): " + GL30.glGetShaderInfoLog(
                        shaderId,
                        1024
                    )
                )
            }
            GL30.glAttachShader(programId, shaderId)
            shader.shaderId = shaderId
        }
        attributes.forEachIndexed { index, attribute ->
            GL30.glBindAttribLocation(programId, index, attribute)
        }
        GL30.glLinkProgram(programId)
    }

    override fun onResourceManagerReload(pResourceManager: ResourceManager) {
        shaders.forEach {
            GL30.glDetachShader(programId, it.shaderId)
            GL30.glDeleteShader(it.shaderId)
        }
        GL30.glDeleteProgram(programId)
        compile()
    }
}

class Shader(val type: Type, val source: ResourceLocation) {
    var shaderId: Int = -1

    enum class Type(val id: Int) {
        VERTEX(GL30.GL_VERTEX_SHADER),
        FRAGMENT(GL30.GL_FRAGMENT_SHADER),
        GEOMETRY(GL33.GL_GEOMETRY_SHADER),
        TESS_CONTROL(GL40.GL_TESS_CONTROL_SHADER),
        TESS_EVALUATION(GL40.GL_TESS_EVALUATION_SHADER),
        COMPUTE(GL43.GL_COMPUTE_SHADER)
    }
}

class AttributeContext(val shader: ShaderProgram) {
    val vao = GPUMemoryManager.createVAO()

    fun indices(indices: IntArray) {
        vao.createIndexBuffer(indices)
    }

    @JvmName("indicesBuffer")
    fun indices(vararg indices: Int) {
        vao.createIndexBuffer(indices)
    }

    operator fun String.invoke(data: FloatArray, type: Type) {
        vao.createAttribute(shader.attributes.size, data, type.id)
        shader.attributes += this
    }

    operator fun String.invoke(data: IntArray, type: Type) {
        vao.createAttribute(shader.attributes.size, data, type.id)
        shader.attributes += this
    }

    operator fun String.invoke(type: Type, vararg data: Float) {
        vao.createAttribute(shader.attributes.size, data, type.id)
        shader.attributes += this
    }

    operator fun String.invoke(type: Type, vararg data: Int) {
        vao.createAttribute(shader.attributes.size, data, type.id)
        shader.attributes += this
    }

    enum class Type(val id: Int) {
        VEC2(3),
        VEC3(3),
        VEC4(4),
    }
}

class UniformContext(val shader: ShaderProgram) {
    operator fun String.invoke(value: Int) = shader.uniforms[this]?.let { GL30.glUniform1i(it, value) }
    operator fun String.invoke(value: Float) = shader.uniforms[this]?.let { GL30.glUniform1f(it, value) }
    operator fun String.invoke(value: IntArray) = shader.uniforms[this]?.let { GL30.glUniform1iv(it, value) }
    operator fun String.invoke(value: FloatArray) = shader.uniforms[this]?.let { GL30.glUniform1fv(it, value) }
    operator fun String.invoke(value: IntBuffer) = shader.uniforms[this]?.let { GL30.glUniform1iv(it, value) }
    operator fun String.invoke(value: FloatBuffer) = shader.uniforms[this]?.let { GL30.glUniform1fv(it, value) }
    operator fun String.invoke(value: Matrix4f) = shader.uniforms[this]?.let {
        GL30.glUniformMatrix4fv(it, false, FloatBuffer.allocate(16).apply(value::store))
    }

    operator fun String.invoke(value: Matrix3f) = shader.uniforms[this]?.let {
        GL30.glUniformMatrix3fv(it, false, FloatBuffer.allocate(9).apply(value::store))
    }

    operator fun String.invoke(vararg values: Int) =
        values.forEachIndexed { i, v -> shader.uniforms[this]?.let { GL30.glUniform1i(it, v) } }

    operator fun String.invoke(vararg values: Float) =
        values.forEachIndexed { i, v -> shader.uniforms[this]?.let { GL30.glUniform1f(it, v) } }

    operator fun String.invoke(vararg values: Matrix4f) =
        values.forEachIndexed { i, v ->
            shader.uniforms[this]?.let {
                GL30.glUniformMatrix4fv(
                    it,
                    false,
                    FloatBuffer.allocate(16).apply(v::store)
                )
            }
        }

    operator fun String.invoke(vararg values: Matrix3f) =
        values.forEachIndexed { i, v ->
            shader.uniforms[this]?.let {
                GL30.glUniformMatrix3fv(
                    it,
                    false,
                    FloatBuffer.allocate(9).apply(v::store)
                )
            }
        }
}