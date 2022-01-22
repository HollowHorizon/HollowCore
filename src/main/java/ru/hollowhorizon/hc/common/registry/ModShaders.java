package ru.hollowhorizon.hc.common.registry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.handlers.ClientTickHandler;
import ru.hollowhorizon.hc.client.render.shader.PostShader;
import ru.hollowhorizon.hc.client.render.shader.ShaderProgram;
import ru.hollowhorizon.hc.client.render.shader.ShaderProgramBuilder;
import ru.hollowhorizon.hc.client.render.shader.UniformType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static ru.hollowhorizon.hc.HollowCore.MODID;
import static ru.hollowhorizon.hc.client.render.shader.ShaderObject.StandardShaderType.FRAGMENT;

public class ModShaders implements ISelectiveResourceReloadListener {
    protected static final List<PostShader> SHADERS = new ArrayList<>();
    protected static PostShader square_overlay;

    public void clear()
    {
        SHADERS.forEach(ShaderGroup::close);
        SHADERS.clear();
    }

    public static ShaderProgram squareOverlay = ShaderProgramBuilder.builder()
            .addShader("square_overlay", shader -> shader
                    .type(FRAGMENT)
                    .source(new ResourceLocation(MODID, "shaders/square_overlay.frag"))
                    .uniform("hazeRed", UniformType.INT)
                    .uniform("hazeGreen", UniformType.INT)
                    .uniform("hazeBlue", UniformType.INT)
                    .uniform("time", UniformType.INT)
                    .uniform("intensity", UniformType.FLOAT)
                    .uniform("alpha", UniformType.FLOAT)
                    .uniform("windowX", UniformType.INT)
                    .uniform("windowY", UniformType.INT)
                    .uniform("image", UniformType.INT)
            )
            .whenUsed((cache -> {
                cache.glUniform1i("time", ClientTickHandler.ticksInGame);
                cache.glUniform1f("alpha", 1f);
                cache.glUniform1i("windowX", Minecraft.getInstance().getWindow().getGuiScaledWidth());
                cache.glUniform1i("windowY", Minecraft.getInstance().getWindow().getGuiScaledHeight());
                cache.glUniform1i("hazeRed", 255);
                cache.glUniform1i("hazeGreen", 255);
                cache.glUniform1i("hazeBlue", 255);
                cache.glUniform1f("intensity", 1f);
            }))
            .build();

    public static ShaderProgram pixelization = ShaderProgramBuilder.builder()
            .addShader("gainprogress2", shader -> shader
                    .type(FRAGMENT)
                    .source(new ResourceLocation(MODID, "shaders/gainprogress2.frag"))
                    .uniform("time", UniformType.INT)
                    .uniform("intensity", UniformType.FLOAT)
                    .uniform("alpha", UniformType.FLOAT)
                    .uniform("windowX", UniformType.INT)
                    .uniform("windowY", UniformType.INT)
            )
            .build();

    public static ShaderProgram blur = ShaderProgramBuilder.builder()
            .addShader("blur", shader -> shader
                    .type(FRAGMENT)
                    .source(new ResourceLocation(MODID, "shaders/blur.frag"))
                    .uniform("time", UniformType.INT)
                    .uniform("intensity", UniformType.FLOAT)
                    .uniform("alpha", UniformType.FLOAT)
                    .uniform("windowX", UniformType.INT)
                    .uniform("windowY", UniformType.INT)
            )
            .whenUsed((cache -> {
                cache.glUniform1i("time", ClientTickHandler.ticksInGame);
                cache.glUniform1f("alpha", 1f);
                cache.glUniform1i("windowX", Minecraft.getInstance().getWindow().getGuiScaledWidth());
                cache.glUniform1i("windowY", Minecraft.getInstance().getWindow().getGuiScaledHeight());
            }))
            .build();

    public static ShaderProgram portal = ShaderProgramBuilder.builder()
            .addShader("frag", shader -> shader
                    .type(FRAGMENT)
                    .source(new ResourceLocation(MODID, "shaders/portal.frag"))
                    .uniform("time", UniformType.INT)
                    .uniform("intensity", UniformType.FLOAT)
                    .uniform("alpha", UniformType.FLOAT)
                    .uniform("windowX", UniformType.INT)
                    .uniform("windowY", UniformType.INT)
            )
            .whenUsed((cache -> {
                cache.glUniform1i("time", ClientTickHandler.ticksInGame);
                cache.glUniform1f("alpha", 1f);
                cache.glUniform1i("windowX", Minecraft.getInstance().getWindow().getGuiScaledWidth());
                cache.glUniform1i("windowY", Minecraft.getInstance().getWindow().getGuiScaledHeight());
            }))
            .build();

    public static ShaderProgram alpha = ShaderProgramBuilder.builder()
            .addShader("frag", shader -> shader
                    .type(FRAGMENT)
                    .source(new ResourceLocation(MODID, "shaders/alfa.frag"))
                    .uniform("time", UniformType.INT)
                    .uniform("intensity", UniformType.FLOAT)
                    .uniform("alpha", UniformType.FLOAT)
                    .uniform("windowX", UniformType.INT)
                    .uniform("windowY", UniformType.INT)
            )
            .whenUsed((cache -> {
                cache.glUniform1i("time", ClientTickHandler.ticksInGame);
                cache.glUniform1f("alpha", 1f);
                cache.glUniform1i("windowX", Minecraft.getInstance().getWindow().getGuiScaledWidth());
                cache.glUniform1i("windowY", Minecraft.getInstance().getWindow().getGuiScaledHeight());
            }))
            .build();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
        this.clear();
        try {
            init(resourceManager);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void init(IResourceManager mgr) throws IOException {
        HollowCore.LOGGER.info("<shader init>");
        square_overlay = add(new PostShader(MODID, "blur"));
    }

    public static PostShader add(PostShader shader) {
        SHADERS.add(shader);
        return shader;
    }

    public static PostShader getSquareOverlay() {
        return square_overlay;
    }
}
