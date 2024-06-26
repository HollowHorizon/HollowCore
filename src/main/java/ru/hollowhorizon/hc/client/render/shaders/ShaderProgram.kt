package ru.hollowhorizon.hc.client.render.shaders

import net.minecraft.resources.ResourceLocation
import org.lwjgl.opengl.GL20
import ru.hollowhorizon.hc.HollowCore

class ShaderProgram(shaders: List<ResourceLocation>, val uniforms: List<Uniform>) {

    val id = GL20.glCreateProgram()

    init {
        shaders.map(::Shader).forEach {
            GL20.glAttachShader(id, it.id)
            GL20.glDeleteShader(it.id)
        }

        GL20.glLinkProgram(id)
        if (GL20.glGetProgrami(id, GL20.GL_LINK_STATUS) == 0) {
            HollowCore.LOGGER.error(
                "Error encountered when linking program containing: {}",
                shaders.joinToString(", ")
            )
            HollowCore.LOGGER.warn(GL20.glGetProgramInfoLog(id, GL20.GL_HINT_BIT))
        }
        uniforms.forEach {
            val id = GL20.glGetUniformLocation(id, it.name)
            if (id == -1) {
                HollowCore.LOGGER.warn("Uniform ${it.name} does not exist!")
            } else {
                it.id = id
            }
        }
    }

    operator fun get(uniform: String) = uniforms.firstOrNull { it.name == uniform }

    fun bind() {
        GL20.glUseProgram(id)
    }

    fun unbind() {
        GL20.glUseProgram(0)
    }

    fun destroy() {
        GL20.glDeleteProgram(id)
    }
}