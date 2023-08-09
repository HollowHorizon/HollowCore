package ru.hollowhorizon.hc.client.gltf;

import com.mojang.blaze3d.systems.RenderSystem;
import de.javagl.jgltf.model.io.Buffers;
import de.javagl.jgltf.model.io.GltfModelReader;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.fml.ModList;
import org.apache.commons.io.IOUtils;
import org.lwjgl.opengl.*;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class GlTFModelManager {
    private static GlTFModelManager INSTANCE;
    private final Map<ResourceLocation, Supplier<ByteBuffer>> loadedBufferResources = new HashMap<>();
    private final Map<String, Supplier<ByteBuffer>> loadedImageResources = new HashMap<>();
    private final Map<String, RenderedGltfModel> renderedModels = new HashMap<>();
    private final boolean isOptiFineExist;
    private int glProgramSkinning = -1;
    private int defaultColorMap;
    private int defaultNormalMap;

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

                GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);

                RenderedGltfModel model = new RenderedGltfModelGL40(new GltfModelReader().readWithoutReferences(new BufferedInputStream(GLTFAdapter.prepare(GltfModelSources.getStream(modelPath)))));

                GL15.glBindBuffer(GL30.GL_TRANSFORM_FEEDBACK_BUFFER, 0);
                GL15.glBindBuffer(GL43.GL_SHADER_STORAGE_BUFFER, 0);
                GL30.glBindVertexArray(0);
                GL40.glBindTransformFeedback(GL40.GL_TRANSFORM_FEEDBACK, 0);

                GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
                GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);

                GL11.glPopAttrib();

                return model;
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        });
    }

    public static void clientSetup(RegisterClientReloadListenersEvent event) {
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

        GL11.glPixelStorei(GL11.GL_UNPACK_ROW_LENGTH, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_ROWS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_SKIP_PIXELS, 0);
        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);

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
}
