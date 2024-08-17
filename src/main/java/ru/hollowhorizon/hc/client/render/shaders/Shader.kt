/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.render.shaders

import net.minecraft.resources.ResourceLocation
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL33
import org.lwjgl.opengl.GL43
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.stream

class Shader(val location: ResourceLocation) {
    val id: Int

    init {
        val type = location.path.substringAfterLast(".")
        val shaderType =
            Type.values().find { it.type == type } ?: throw IllegalArgumentException("Unknown shader type: $type")
        id = GL20.glCreateShader(shaderType.id)
        GL20.glShaderSource(id, String(location.stream.readBytes()))
        GL20.glCompileShader(id)
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 0) {
            val error = GL20.glGetShaderInfoLog(id, GL20.GL_HINT_BIT)

            HollowCore.LOGGER.error("Error compiling shader ($location): $error")
        }
    }

    enum class Type(val id: Int, val type: String) {
        VERTEX(GL20.GL_VERTEX_SHADER, "vsh"),
        FRAGMENT(GL20.GL_FRAGMENT_SHADER, "fsh"),
        GEOMETRY(GL33.GL_GEOMETRY_SHADER, "gsh"),
        COMPUTE(GL43.GL_COMPUTE_SHADER, "csh")
    }
}