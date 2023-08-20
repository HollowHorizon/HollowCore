package com.modularmods.mcgltf;

import com.mojang.blaze3d.systems.RenderSystem;
import de.javagl.jgltf.model.io.Buffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MCglTF {

    public static final String MODID = "mcgltf";
    public static final String RESOURCE_LOCATION = "resourceLocation";

    public static final Logger logger = LogManager.getLogger(MODID);

    private static MCglTF INSTANCE;

    private final Pair<CompatibilityConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CompatibilityConfig::new);
    private final Map<ResourceLocation, Supplier<ByteBuffer>> loadedBufferResources = new HashMap<ResourceLocation, Supplier<ByteBuffer>>();
    private final Map<ResourceLocation, Supplier<ByteBuffer>> loadedImageResources = new HashMap<ResourceLocation, Supplier<ByteBuffer>>();
    private final Map<ResourceLocation, RenderedGltfModel> gltfModels = new HashMap<>();
    public final List<Runnable> gltfRenderData = new ArrayList<Runnable>();
    private final boolean isOptiFineExist;
    private int glProgramSkinnig = -1;
    private int defaultColorMap;
    private int defaultNormalMap;
    private AbstractTexture lightTexture;

    public MCglTF() {
        INSTANCE = this;

        Class<?> clazz = null;
        try {
            clazz = Class.forName("net.optifine.shaders.Shaders");
        } catch (ClassNotFoundException ignored) {
        }
        isOptiFineExist = clazz != null;

        RenderSystem.recordRenderCall(() -> INSTANCE.createSkinningProgramGL43());
    }

    public static RenderedGltfModel getOrCreate(ResourceLocation location) {
        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

        int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        var model = INSTANCE.gltfModels.computeIfAbsent(location, RenderedGltfModel::new);

        GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0);
        GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
        GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);

        return model;
    }

    //Configs from ForgeConfigSpec is not yet loaded at this event.
    @SubscribeEvent
    public static void onEvent(RegisterClientReloadListenersEvent event) {
        INSTANCE.lightTexture = Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation("dynamic/light_map_1"));

        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

        int currentTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

        INSTANCE.defaultColorMap = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, INSTANCE.defaultColorMap);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, Buffers.create(new byte[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1}));
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);

        INSTANCE.defaultNormalMap = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, INSTANCE.defaultNormalMap);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, Buffers.create(new byte[]{-128, -128, -1, -1, -128, -128, -1, -1, -128, -128, -1, -1, -128, -128, -1, -1}));
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_BASE_LEVEL, 0);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL12.GL_TEXTURE_MAX_LEVEL, 0);

        GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture);

        event.registerReloadListener((ResourceManagerReloadListener) p_10758_ -> {
            INSTANCE.gltfRenderData.forEach(Runnable::run);
            INSTANCE.gltfRenderData.clear();
            INSTANCE.gltfModels.clear();
            INSTANCE.loadedBufferResources.clear();
            INSTANCE.loadedImageResources.clear();
        });
    }

    public static MCglTF getInstance() {
        return INSTANCE;
    }

    public int getGlProgramSkinnig() {
        return glProgramSkinnig;
    }

    public int getDefaultColorMap() {
        return defaultColorMap;
    }

    public int getDefaultNormalMap() {
        return defaultNormalMap;
    }

    public int getDefaultSpecularMap() {
        return 0;
    }

    public AbstractTexture getLightTexture() {
        return lightTexture;
    }

    public ByteBuffer getBufferResource(ResourceLocation location) {
        Supplier<ByteBuffer> supplier;
        synchronized (loadedBufferResources) {
            supplier = loadedBufferResources.get(location);
            if (supplier == null) {
                supplier = new Supplier<ByteBuffer>() {
                    ByteBuffer bufferData;

                    @Override
                    public synchronized ByteBuffer get() {
                        if (bufferData == null) {
                            try {
                                bufferData = Buffers.create(IOUtils.toByteArray(new BufferedInputStream(Minecraft.getInstance().getResourceManager().getResource(location).orElseThrow().open())));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return bufferData;
                    }

                };
                loadedBufferResources.put(location, supplier);
            }
        }
        return supplier.get();
    }

    public ByteBuffer getImageResource(ResourceLocation location) {
        Supplier<ByteBuffer> supplier;
        synchronized (loadedImageResources) {
            supplier = loadedImageResources.get(location);
            if (supplier == null) {
                supplier = new Supplier<ByteBuffer>() {
                    ByteBuffer bufferData;

                    @Override
                    public synchronized ByteBuffer get() {
                        if (bufferData == null) {
                            try {
                                bufferData = Buffers.create(IOUtils.toByteArray(new BufferedInputStream(Minecraft.getInstance().getResourceManager().getResource(location).orElseThrow().open())));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        return bufferData;
                    }

                };
                loadedImageResources.put(location, supplier);
            }
        }
        return supplier.get();
    }

    public boolean isShaderModActive() {
        return isOptiFineExist;
    }

    private void createSkinningProgramGL43() {
        int glShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(glShader,
                "#version 430\r\n"
                        + "layout(location = 0) in vec4 joint;"
                        + "layout(location = 1) in vec4 weight;"
                        + "layout(location = 2) in vec3 position;"
                        + "layout(location = 3) in vec3 normal;"
                        + "layout(location = 4) in vec4 tangent;"
                        + "layout(std430, binding = 0) readonly buffer jointMatrixBuffer {mat4 jointMatrices[];};"
                        + "out vec3 outPosition;"
                        + "out vec3 outNormal;"
                        + "out vec4 outTangent;"
                        + "void main() {"
                        + "mat4 skinMatrix ="
                        + " weight.x * jointMatrices[int(joint.x)] +"
                        + " weight.y * jointMatrices[int(joint.y)] +"
                        + " weight.z * jointMatrices[int(joint.z)] +"
                        + " weight.w * jointMatrices[int(joint.w)];"
                        + "outPosition = (skinMatrix * vec4(position, 1.0)).xyz;"
                        + "mat3 upperLeft = mat3(skinMatrix);"
                        + "outNormal = upperLeft * normal;"
                        + "outTangent.xyz = upperLeft * tangent.xyz;"
                        + "outTangent.w = tangent.w;"
                        + "}");
        GL20.glCompileShader(glShader);

        glProgramSkinnig = GL20.glCreateProgram();
        GL20.glAttachShader(glProgramSkinnig, glShader);
        GL20.glDeleteShader(glShader);
        GL30.glTransformFeedbackVaryings(glProgramSkinnig, new CharSequence[]{"outPosition", "outNormal", "outTangent"}, GL30.GL_SEPARATE_ATTRIBS);
        GL20.glLinkProgram(glProgramSkinnig);
    }

    private void createSkinningProgramGL33() {
        int glShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(glShader,
                "#version 330\r\n"
                        + "layout(location = 0) in vec4 joint;"
                        + "layout(location = 1) in vec4 weight;"
                        + "layout(location = 2) in vec3 position;"
                        + "layout(location = 3) in vec3 normal;"
                        + "layout(location = 4) in vec4 tangent;"
                        + "uniform samplerBuffer jointMatrices;"
                        + "out vec3 outPosition;"
                        + "out vec3 outNormal;"
                        + "out vec4 outTangent;"
                        + "void main() {"
                        + "int jx = int(joint.x) * 4;"
                        + "int jy = int(joint.y) * 4;"
                        + "int jz = int(joint.z) * 4;"
                        + "int jw = int(joint.w) * 4;"
                        + "mat4 skinMatrix ="
                        + " weight.x * mat4(texelFetch(jointMatrices, jx), texelFetch(jointMatrices, jx + 1), texelFetch(jointMatrices, jx + 2), texelFetch(jointMatrices, jx + 3)) +"
                        + " weight.y * mat4(texelFetch(jointMatrices, jy), texelFetch(jointMatrices, jy + 1), texelFetch(jointMatrices, jy + 2), texelFetch(jointMatrices, jy + 3)) +"
                        + " weight.z * mat4(texelFetch(jointMatrices, jz), texelFetch(jointMatrices, jz + 1), texelFetch(jointMatrices, jz + 2), texelFetch(jointMatrices, jz + 3)) +"
                        + " weight.w * mat4(texelFetch(jointMatrices, jw), texelFetch(jointMatrices, jw + 1), texelFetch(jointMatrices, jw + 2), texelFetch(jointMatrices, jw + 3));"
                        + "outPosition = (skinMatrix * vec4(position, 1.0)).xyz;"
                        + "mat3 upperLeft = mat3(skinMatrix);"
                        + "outNormal = upperLeft * normal;"
                        + "outTangent.xyz = upperLeft * tangent.xyz;"
                        + "outTangent.w = tangent.w;"
                        + "}");
        GL20.glCompileShader(glShader);

        glProgramSkinnig = GL20.glCreateProgram();
        GL20.glAttachShader(glProgramSkinnig, glShader);
        GL20.glDeleteShader(glShader);
        GL30.glTransformFeedbackVaryings(glProgramSkinnig, new CharSequence[]{"outPosition", "outNormal", "outTangent"}, GL30.GL_SEPARATE_ATTRIBS);
        GL20.glLinkProgram(glProgramSkinnig);
    }

    public EnumRenderedModelGLProfile getRenderedModelGLProfile() {
        return specPair.getLeft().renderedModelGLProfile.get();
    }

    public enum EnumRenderedModelGLProfile {
        AUTO,
        GL43,
        GL40,
        GL33,
        GL30
    }

    static class CompatibilityConfig {
        final EnumValue<EnumRenderedModelGLProfile> renderedModelGLProfile;

        CompatibilityConfig(ForgeConfigSpec.Builder builder) {
            builder.comment("Client-only settings")
                    .push("compatibility");
            renderedModelGLProfile = builder.comment("Set maximum version of OpenGL to enable some optimizations for rendering glTF model.",
                            "The AUTO means it will select maximum OpenGL version available based on your hardware. The GL43 is highest it may select.",
                            "The lower OpenGL version you set, the more negative impact on performance you will probably get.",
                            "The GL30 is a special profile which essentially the GL33 and above but replace hardware(GPU) skinning with software(CPU) skinning. This will trade a lots of CPU performance for a few GPU performance increase.")
                    .defineEnum("RenderedModelGLProfile", EnumRenderedModelGLProfile.AUTO);
            builder.pop();
        }
    }

}
