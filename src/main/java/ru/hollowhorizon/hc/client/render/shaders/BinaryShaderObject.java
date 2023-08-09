package ru.hollowhorizon.hc.client.render.shaders;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL46;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.function.Consumer;

public class BinaryShaderObject extends NamedShaderObject implements ResourceManagerReloadListener {

    private static final Logger LOGGER = LogManager.getLogger();

    private final ResourceLocation asset;
    private final BinaryType binaryType;
    private final String entryPoint;
    private final ShaderConstantCache constantCache = new ShaderConstantCache();
    private final Consumer<ConstantCache> specializationCallback;
    private boolean dirty = false;
    private int shaderId = -1;

    public BinaryShaderObject(String name, ResourceLocation asset, ShaderType type, BinaryType binaryType, String entryPoint, Collection<Uniform> uniforms, Consumer<ConstantCache> specializationCallback) {
        super(name, type, uniforms);
        this.asset = asset;
        this.binaryType = binaryType;
        this.entryPoint = entryPoint;
        this.specializationCallback = specializationCallback;
    }

    @Override
    public boolean isDirty() {
        return dirty;
    }

    @Override
    public void alloc() {
        if (dirty || shaderId == -1) {
            if (shaderId == -1) {
                shaderId = GL20.glCreateShader(getShaderType().getGLCode());
                if (shaderId == 0) {
                    throw new IllegalStateException("Allocation of ShaderObject failed.");
                }
            }

            byte[] source = getRawSource();
            ByteBuffer sourceBuffer = BufferUtils.createByteBuffer(source.length).order(ByteOrder.nativeOrder());
            sourceBuffer.put(source);
            sourceBuffer.flip();
            GL46.glShaderBinary(new int[]{shaderId}, binaryType.getGLCode(), sourceBuffer);

            specializationCallback.accept(constantCache);
            GL46.glSpecializeShader(shaderId, entryPoint, constantCache.getIndices(), constantCache.getValues());

            if (GL20.glGetShaderi(shaderId, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
                throw new RuntimeException("ShaderProgram linkage failure. \n" + GL20.glGetShaderInfoLog(shaderId));
            }

            dirty = false;
        }
    }

    @Override
    public int getShaderID() {
        return shaderId;
    }

    @Override
    public void onLink(int programId) {
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        dirty = true;
    }

    private byte[] getRawSource() {
        try {
            Resource resource = Minecraft.getInstance().getResourceManager().getResource(asset).orElseThrow();
            try (InputStream istream = resource.open()) {
                ByteArrayOutputStream ostream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int read;
                while ((read = istream.read(buffer)) != -1) {
                    ostream.write(buffer, 0, read);
                }
                return ostream.toByteArray();
            }
        } catch (Throwable t) {
            LOGGER.error("Failed to read shader asset.", t);
            return new byte[0];
        }
    }

}
