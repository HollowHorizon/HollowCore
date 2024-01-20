package ru.hollowhorizon.hc.client.models.gltf.manager

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL12
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.client.models.gltf.GltfModel
import ru.hollowhorizon.hc.client.models.gltf.GltfTree
import java.nio.ByteBuffer
import java.nio.ByteOrder


object GltfManager {
    lateinit var lightTexture: AbstractTexture
    private val models = HashMap<ResourceLocation, GltfModel>()
    var glProgramSkinning = -1

    fun getOrCreate(location: ResourceLocation) = models.computeIfAbsent(location) { model ->
        GltfModel(GltfTree.parse(model))
    }

    @JvmStatic
    fun onReload(event: RegisterClientReloadListenersEvent) {
        lightTexture = Minecraft.getInstance().getTextureManager().getTexture(ResourceLocation("dynamic/light_map_1"))

        val currentTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D)

        val defaultColorMap = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, defaultColorMap)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, create(
                byteArrayOf(-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1)
            )
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0)

        Minecraft.getInstance().textureManager.register(ResourceLocation("hc:default_color_map"), object : AbstractTexture() {
            init {
                id = defaultColorMap
            }

            override fun load(p0: ResourceManager) {}
        })

        val defaultNormalMap = GL11.glGenTextures()
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, defaultNormalMap)
        GL11.glTexImage2D(
            GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, create(
                byteArrayOf(-128, -128, -1, -1, -128, -128, -1, -1, -128, -128, -1, -1, -128, -128, -1, -1)
            )
        )
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0)

        Minecraft.getInstance().textureManager.register(ResourceLocation("hc:default_normal_map"), object : AbstractTexture() {
            init {
                id = defaultNormalMap
            }

            override fun load(p0: ResourceManager) {}
        })

        Minecraft.getInstance().textureManager.register(ResourceLocation("hc:default_specular_map"), object : AbstractTexture() {
            init {
                id = 0
            }

            override fun load(p0: ResourceManager) {}
        })

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture)

        createSkinningProgramGL33()

        event.registerReloadListener(ResourceManagerReloadListener {
            models.values.forEach { it.destroy() }
            models.clear()
        })
    }

    private fun createSkinningProgramGL33() {
        val glShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER)
        GL20.glShaderSource(
            glShader,
            """
            #version 330
            layout(location = 0) in vec4 joint;
            layout(location = 1) in vec4 weight;
            layout(location = 2) in vec3 position;
            layout(location = 3) in vec3 normal;
            uniform samplerBuffer jointMatrices;
            out vec3 outPosition;
            out vec3 outNormal;
            void main() {
                int jx = int(joint.x) * 4;
                int jy = int(joint.y) * 4;
                int jz = int(joint.z) * 4;
                int jw = int(joint.w) * 4;
                mat4 skinMatrix = weight.x * mat4(
                    texelFetch(jointMatrices, jx), 
                    texelFetch(jointMatrices, jx + 1), 
                    texelFetch(jointMatrices, jx + 2), 
                    texelFetch(jointMatrices, jx + 3)
                ) + weight.y * mat4(
                    texelFetch(jointMatrices, jy), 
                    texelFetch(jointMatrices, jy + 1), 
                    texelFetch(jointMatrices, jy + 2), 
                    texelFetch(jointMatrices, jy + 3)
                ) + weight.z * mat4(
                    texelFetch(jointMatrices, jz), 
                    texelFetch(jointMatrices, jz + 1), 
                    texelFetch(jointMatrices, jz + 2), 
                    texelFetch(jointMatrices, jz + 3)
                ) + weight.w * mat4(
                    texelFetch(jointMatrices, jw), 
                    texelFetch(jointMatrices, jw + 1), 
                    texelFetch(jointMatrices, jw + 2), 
                    texelFetch(jointMatrices, jw + 3)
                );
                outPosition = (skinMatrix * vec4(position, 1.0)).xyz;
                mat3 upperLeft = mat3(skinMatrix);
                outNormal = upperLeft * normal;
            }
            """.trimIndent()
        )
        GL20.glCompileShader(glShader)

        glProgramSkinning = GL20.glCreateProgram()
        GL20.glAttachShader(glProgramSkinning, glShader)
        GL20.glDeleteShader(glShader)
        GL30.glTransformFeedbackVaryings(
            glProgramSkinning,
            arrayOf<CharSequence>("outPosition", "outNormal"),
            GL30.GL_SEPARATE_ATTRIBS
        )
        GL20.glLinkProgram(glProgramSkinning)
    }
}

fun create(data: ByteArray): ByteBuffer {
    return create(data, 0, data.size)
}

fun create(data: ByteArray?, offset: Int, length: Int): ByteBuffer {
    val byteBuffer = ByteBuffer.allocateDirect(length)
    byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
    byteBuffer.put(data, offset, length)
    byteBuffer.position(0)
    return byteBuffer
}