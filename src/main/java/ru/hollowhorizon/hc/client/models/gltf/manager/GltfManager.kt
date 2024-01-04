package ru.hollowhorizon.hc.client.models.gltf.manager

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.AbstractTexture
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import ru.hollowhorizon.hc.client.models.gltf.GltfModel
import ru.hollowhorizon.hc.client.models.gltf.GltfTree


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

        createSkinningProgramGL33()

        event.registerReloadListener(ResourceManagerReloadListener {
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