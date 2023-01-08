package ru.hollowhorizon.hc.common.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
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

    public static void init(AddReloadListenerEvent e) {
        HollowCore.LOGGER.info("init all shaders");
        initShader(TEST_SHADER, e);
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
