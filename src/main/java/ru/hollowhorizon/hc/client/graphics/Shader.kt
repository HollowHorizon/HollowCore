package ru.hollowhorizon.hc.client.graphics

import com.mojang.blaze3d.platform.GlStateManager
import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.stream
import java.nio.FloatBuffer
import java.util.stream.Collectors


class Shader(fragmentShaderName: String) {
    private val programId: Int

    init {
        val programId = GlStateManager.glCreateProgram()
        try {
            val fragmentShader = GlStateManager.glCreateShader(GL30.GL_FRAGMENT_SHADER)
            GlStateManager.glShaderSource(fragmentShader, mutableListOf(getShaderSource(fragmentShaderName)))
            GlStateManager.glCompileShader(fragmentShader)
            val isFragmentCompiled = GL30.glGetShaderi(fragmentShader, GL30.GL_COMPILE_STATUS)
            if (isFragmentCompiled == 0) {
                GlStateManager.glDeleteShader(fragmentShader)
                System.err.println("Fragment shader couldn't compile. It has been deleted.")
            }
            GlStateManager.glAttachShader(programId, VERTEX_SHADER)
            GlStateManager.glAttachShader(programId, fragmentShader)
            GlStateManager.glLinkProgram(programId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        this.programId = programId
    }

    fun load() {
        GlStateManager._glUseProgram(programId)
    }

    fun unload() {
        GlStateManager._glUseProgram(0)
    }

    fun getUniform(name: String?): Int {
        return GL30.glGetUniformLocation(programId, name!!)
    }

    fun setUniformf(name: String?, vararg args: Float) {
        val loc = GL30.glGetUniformLocation(programId, name!!)
        when (args.size) {
            1 -> GL30.glUniform1f(loc, args[0])
            2 -> GL30.glUniform2f(loc, args[0], args[1])
            3 -> GL30.glUniform3f(loc, args[0], args[1], args[2])
            4 -> GL30.glUniform4f(loc, args[0], args[1], args[2], args[3])
        }
    }

    fun setUniformi(name: String?, vararg args: Int) {
        val loc = GL30.glGetUniformLocation(programId, name!!)
        when (args.size) {
            1 -> GL30.glUniform1i(loc, args[0])
            2 -> GL30.glUniform2i(loc, args[0], args[1])
            3 -> GL30.glUniform3i(loc, args[0], args[1], args[2])
            4 -> GL30.glUniform4i(loc, args[0], args[1], args[2], args[3])
        }
    }

    fun setUniformfb(name: String?, buffer: FloatBuffer?) {
        GL30.glUniform1fv(GL30.glGetUniformLocation(programId, name!!), buffer!!)
    }

    companion object {
        val VERTEX_SHADER = GlStateManager.glCreateShader(GL30.GL_VERTEX_SHADER)

        init {
            GlStateManager.glShaderSource(VERTEX_SHADER, mutableListOf(getShaderSource("empty.vert")))
            GlStateManager.glCompileShader(VERTEX_SHADER)
        }

        @JvmOverloads
        fun draw(
            x: Double = 0.0,
            y: Double = 0.0,
            width: Int = Minecraft.getInstance().window.guiScaledWidth,
            height: Int = Minecraft.getInstance().window.guiScaledHeight,
        ) {

//            val builder = Tesselator.getInstance().builder
//            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN)
//            builder.vertex(x, y, 0.0)
//            builder.uv(0.0f, 0.0f)
//            builder.vertex(x, y+height, 0.0)
//            builder.uv(0.0f, 1.0f)
//            builder.vertex(x+width, y+height, 0.0)
//            builder.uv(1.0f, 1.0f)
//            builder.vertex(x + width, y, 0.0)
//            builder.uv(1.0f, 0.0f)
//            Tesselator.getInstance().end()
        }

        fun getShaderSource(fileName: String): String {
            val bufferedReader = "${HollowCore.MODID}:shaders/$fileName".rl.stream.reader().buffered()
            val source = bufferedReader.lines()
                .filter { str: String -> str.isNotEmpty() }
                .map { it.replace("\t", "") }
                .collect(Collectors.joining("\n"))
            bufferedReader.close()
            return source
        }
    }
}