package ru.hollowhorizon.hc.client.gltf;

import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.GltfModelReader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.fml.ModList;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.*;
import ru.hollowhorizon.hc.client.render.OpenGLUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class GlTFModelManager {
    private static GlTFModelManager INSTANCE;
    public final List<Runnable> gltfRenderData = new ArrayList<>();
    private final Map<ResourceLocation, Supplier<ByteBuffer>> loadedBufferResources = new HashMap<>();
    private final Map<String, Supplier<ByteBuffer>> loadedImageResources = new HashMap<>();
    private final Map<String, RenderedGltfModel> renderedModels = new HashMap<>();
    private final boolean isOptiFineExist;
    private int glProgramSkinning = -1;
    private int defaultColorMap;
    private int defaultNormalMap;
    private AbstractTexture lightTexture;

    public GlTFModelManager() {
        INSTANCE = this;

        isOptiFineExist = ModList.get().isLoaded("optifine");
    }

    public static RenderedGltfModel getOrCreate(String modelPath) {
        return INSTANCE.renderedModels.computeIfAbsent(modelPath, (path) -> {
            try {
                GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
                GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
                GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
                GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

                int currentTexture1 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);

                var asset = new GltfModelReader().readWithoutReferences(new BufferedInputStream(GLTFAdapter.prepare(GltfModelSources.getStream(modelPath))));
                RenderedGltfModel model = OpenGLUtils.isSupportedGL43 ? new RenderedGltfModelGL40(asset) : new RenderedGltfModelGL33(asset);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
                GL30.glBindVertexArray(0);

                GL11.glBindTexture(GL11.GL_TEXTURE_2D, currentTexture1);

                INSTANCE.loadedBufferResources.clear();
                INSTANCE.loadedImageResources.clear();

                return model;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    public static void clientSetup(RegisterClientReloadListenersEvent event) {
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

        event.registerReloadListener((ResourceManagerReloadListener) manager -> {
            INSTANCE.lightTexture = Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation("dynamic/light_map_1"));

            INSTANCE.renderedModels.clear();
            INSTANCE.gltfRenderData.forEach(Runnable::run);
            INSTANCE.gltfRenderData.clear();

            if (OpenGLUtils.isSupportedGL43) {
                createSkinningProgramGL43();
            } else {
                createSkinningProgramGL33();
            }


        });
    }

    private static void createSkinningProgramGL33() {
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

        INSTANCE.glProgramSkinning = GL20.glCreateProgram();
        GL20.glAttachShader(INSTANCE.glProgramSkinning, glShader);
        GL20.glDeleteShader(glShader);
        GL30.glTransformFeedbackVaryings(INSTANCE.glProgramSkinning, new CharSequence[]{"outPosition", "outNormal", "outTangent"}, GL30.GL_SEPARATE_ATTRIBS);
        GL20.glLinkProgram(INSTANCE.glProgramSkinning);
    }

    private static void createSkinningProgramGL43() {
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

        INSTANCE.glProgramSkinning = GL20.glCreateProgram();
        GL20.glAttachShader(INSTANCE.glProgramSkinning, glShader);
        GL20.glDeleteShader(glShader);
        GL30.glTransformFeedbackVaryings(INSTANCE.glProgramSkinning, new CharSequence[]{"outPosition", "outNormal", "outTangent"}, GL30.GL_SEPARATE_ATTRIBS);
        GL20.glLinkProgram(INSTANCE.glProgramSkinning);
    }

    public static GlTFModelManager getInstance() {
        return INSTANCE;
    }

    public int getGlProgramSkinning() {
        return glProgramSkinning;
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

    public ByteBuffer getBufferResource(ResourceLocation location) {
        Supplier<ByteBuffer> supplier;
        synchronized (loadedBufferResources) {
            supplier = loadedBufferResources.computeIfAbsent(location, l -> new Supplier<ByteBuffer>() {
                ByteBuffer bufferData;

                @Override
                public synchronized ByteBuffer get() {
                    if (bufferData == null) {
                        try {
                            Resource resource = Minecraft.getInstance().getResourceManager().getResource(l).orElseThrow();
                            bufferData = Buffers.create(IOUtils.toByteArray(new BufferedInputStream(resource.open())));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    return bufferData;
                }

            });
        }
        return supplier.get();
    }

    public ByteBuffer getImageResource(String location) {
        Supplier<ByteBuffer> supplier;
        synchronized (loadedImageResources) {
            supplier = loadedImageResources.computeIfAbsent(location, l -> new Supplier<ByteBuffer>() {
                ByteBuffer bufferData;

                @Override
                public synchronized ByteBuffer get() {
                    if (bufferData == null) {
                        try {
                            bufferData = Buffers.create(IOUtils.toByteArray(new BufferedInputStream(GltfModelSources.INSTANCE.getStream(location))));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return bufferData;
                }

            });
        }
        return supplier.get();
    }

    public boolean isShaderModActive() {
        return isOptiFineExist;
    }

    public AbstractTexture getLightTexture() {
        return lightTexture;
    }
}
