package ru.hollowhorizon.hc.common.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.event.AddReloadListenerEvent;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.handlers.ClientTickHandler;
import ru.hollowhorizon.hc.client.render.shaders.*;

import static ru.hollowhorizon.hc.HollowCore.MODID;

public class ModShaders {
    public static final ShaderProgram TEST_SHADER = ShaderProgramBuilder.builder()
            .addShader("test_shader", builder -> {
                builder.source(new ResourceLocation(MODID, "shaders/console.glsl"));
                builder.type(ShaderObject.StandardShaderType.FRAGMENT);

                builder.uniform("t", UniformType.INT);
                builder.uniform("windowX", UniformType.INT);
                builder.uniform("windowY", UniformType.INT);
            })
            .whenUsed((uniformCache -> {
                uniformCache.glUniform1i("t", ClientTickHandler.ticksInGame);
                uniformCache.glUniform1i("windowX", Minecraft.getInstance().getMainRenderTarget().width);
                uniformCache.glUniform1i("windowY", Minecraft.getInstance().getMainRenderTarget().height);
            }))
            .build();

    public static final ShaderProgram ASSIMP_SHADER = ShaderProgramBuilder.builder()
            .attributes("in_position", "in_textureCoords", "in_normal", "in_jointIndices", "in_weights")
            .addShader("assimp_shader_frag", builder -> {
                builder.source(new ResourceLocation(MODID, "shaders/animated_entity_fragment.glsl"));
                builder.type(ShaderObject.StandardShaderType.FRAGMENT);

                builder.uniform("textureSampler", UniformType.INT);
                builder.uniform("overlaySampler", UniformType.INT);
                builder.uniform("lightmapSampler", UniformType.INT);

                builder.uniform("lightMapTextureCoords", UniformType.VEC2);
                builder.uniform("overlayTextureCoords", UniformType.INT);
            })
            .addShader("assimp_shader_vert", builder -> {
                builder.source(new ResourceLocation(MODID, "shaders/animated_entity_vertex.glsl"));
                builder.type(ShaderObject.StandardShaderType.VERTEX);

                builder.uniform("modelViewMatrix", UniformType.MAT4);
                builder.uniform("projectionMatrix", UniformType.MAT4);
                for(int i = 0; i < 50; i++) builder.uniform("jointTransforms["+i+"]", UniformType.MAT4);
            })
            .build();

    public static void init(AddReloadListenerEvent e) {
        HollowCore.LOGGER.info("init all shaders");


        //initShader(ASSIMP_SHADER, e);
    }

    private static void initShader(ShaderProgram program, AddReloadListenerEvent e) {
        e.addListener(program);
        program.getShaders().forEach((shader) -> {
            if (shader instanceof AssetShaderObject) {
                e.addListener((AssetShaderObject) shader);
            } else if (shader instanceof BinaryShaderObject) {
                e.addListener((BinaryShaderObject) shader);
            }
        });
    }
}
