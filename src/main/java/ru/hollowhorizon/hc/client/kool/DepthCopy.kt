package ru.hollowhorizon.hc.client.kool

import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL33
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.render.effekseer.internal.RenderStateCapture
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.stream

object DepthCopy {
    private var quadVao: Int
    private val program: Int = GL30.glCreateProgram()
    private val depthTexture: Int
    private val colorTexture: Int

    init {
        val fragment = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER)
        GL20.glShaderSource(fragment, "hollowcore:shaders/core/copy_depth.fsh".rl.stream.readBytes().decodeToString())
        GL20.glCompileShader(fragment)

        val vertex = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(vertex, "hollowcore:shaders/core/copy_depth.vsh".rl.stream.readBytes().decodeToString())
        GL20.glCompileShader(vertex)

        GL30.glCreateProgram()
        GL20.glAttachShader(program, fragment)
        if(GL20.glGetShaderi(fragment, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            HollowCore.LOGGER.info("Shader compile error: ${GL20.glGetShaderInfoLog(program)}")
        }
        GL20.glDeleteShader(fragment)
        GL20.glAttachShader(program, vertex)
        if(GL20.glGetShaderi(vertex, GL20.GL_COMPILE_STATUS) == GL20.GL_FALSE) {
            HollowCore.LOGGER.info("Shader compile error: ${GL20.glGetShaderInfoLog(program)}")
        }
        GL20.glDeleteShader(vertex)
        GL20.glLinkProgram(program)

        depthTexture = GL30.glGetUniformLocation(program, "depthTexture")
        colorTexture = GL30.glGetUniformLocation(program, "colorTexture")

        quadVao = setupQuad()
    }

    fun setupQuad(): Int {
        val quadVertices = floatArrayOf(
            -1.0f, -1.0f, 0.0f,  0.0f, 0.0f,
            1.0f, -1.0f, 0.0f,  1.0f, 0.0f,
            -1.0f,  1.0f, 0.0f,  0.0f, 1.0f,
            1.0f,  1.0f, 0.0f,  1.0f, 1.0f
        )
        val quadVAO = GL33.glGenVertexArrays()
        val quadVBO = GL33.glGenBuffers()

        GL33.glBindVertexArray(quadVAO);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, quadVBO);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, quadVertices, GL33.GL_STATIC_DRAW);

        // Указываем атрибуты вершин
        GL33.glVertexAttribPointer(0, 3, GL33.GL_FLOAT, false, 5 * 4, 0)
        GL33.glEnableVertexAttribArray(0)
        GL33.glVertexAttribPointer(1, 2, GL33.GL_FLOAT, false, 5 * 4, 3*4)
        GL33.glEnableVertexAttribArray(1)

        return quadVAO
    }

    fun use() {
        val texBind = GL33.glGetInteger(GL33.GL_ACTIVE_TEXTURE)
        val currentProgram = GL30.glGetInteger(GL30.GL_CURRENT_PROGRAM)
        val currentVAO = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING)


        GL30.glUseProgram(program)

        GL13.glActiveTexture(GL13.GL_TEXTURE0)
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, Minecraft.getInstance().mainRenderTarget.depthTextureId)
        GL30.glUniform1i(depthTexture, 0)
        GL13.glActiveTexture(GL13.GL_TEXTURE1)
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, Minecraft.getInstance().mainRenderTarget.colorTextureId)
        GL30.glUniform1i(colorTexture, 1)

        GL30.glBindVertexArray(quadVao)
        GL30.glDrawArrays(GL30.GL_TRIANGLE_STRIP, 0, 4)
        GL30.glBindVertexArray(currentVAO)

        GL30.glUseProgram(currentProgram)
        GL33.glActiveTexture(texBind)
    }
}