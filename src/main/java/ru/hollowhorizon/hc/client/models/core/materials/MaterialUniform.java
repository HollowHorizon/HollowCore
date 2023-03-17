package ru.hollowhorizon.hc.client.models.core.materials;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.shader.IShaderManager;
import net.minecraft.client.shader.ShaderUniform;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryUtil;
import ru.hollowhorizon.hc.HollowCore;
import ru.hollowhorizon.hc.client.utils.math.Matrix4d;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryUtil.memAddress;

@OnlyIn(Dist.CLIENT)
public class MaterialUniform implements AutoCloseable {

    private int uniformLocation;
    private final int uniformCount;
    private int matCount;

    public enum UniformType {
        int1,
        int2,
        int3,
        int4,
        float1,
        float2,
        float3,
        float4,
        mat2x2,
        mat3x3,
        mat4x4,
        vec2f,
        vec2i,
        vec3f,
        vec3i,
        vec4i,
        vec4f,
        vecmat4x4
    }

    public static int getCountForType(UniformType type) {
        switch (type) {
            case int1:
            case float1:
                return 1;
            case int2:
            case float2:
            case vec2f:
            case vec2i:
                return 2;
            case int3:
            case float3:
                return 3;
            case int4:
            case float4:
                return 4;
            case vec3f:
            case vec3i:
                return 3;
            case vec4f:
            case vec4i:
            case mat2x2:
                return 4;
            case mat3x3:
                return 9;
            case mat4x4:
            case vecmat4x4:
                return 16;
            default:
                return 1;
        }
    }

    public static boolean isIntType(UniformType type) {
        switch (type) {
            case int1:
            case int2:
            case int3:
            case int4:
            case vec2i:
            case vec3i:
            case vec4i:
                return true;
            default:
                return false;
        }
    }

    private final UniformType uniformType;
    private final IntBuffer uniformIntBuffer;
    private final FloatBuffer uniformFloatBuffer;
    private final String uniformName;
    private boolean dirty;
    private final IShaderManager shaderManager;

    public MaterialUniform(String name, UniformType type, IShaderManager manager) {
        this(name, type, 1, manager);
    }

    public MaterialUniform(String name, UniformType type, int count, IShaderManager manager) {
        this.uniformName = name;
        this.uniformCount = count;
        this.uniformType = type;
        this.matCount = 1;
        this.shaderManager = manager;
        if (isIntType(type)) {
            this.uniformIntBuffer = MemoryUtil.memAllocInt(count * getCountForType(type));
            this.uniformFloatBuffer = null;
        } else {
            this.uniformIntBuffer = null;
            this.uniformFloatBuffer = MemoryUtil.memAllocFloat(count * getCountForType(type));
        }
        this.uniformLocation = -1;
        this.markDirty();
    }

    public void bindUniform(int programId) {
        int loc = ShaderUniform.glGetUniformLocation(programId, this.getUniformName());
        if (loc == -1) {
            HollowCore.LOGGER.warn("Error trying to bind loc for {}", this.getUniformName());
        } else {
            HollowCore.LOGGER.info("Loc for {}, {}: {}", programId, this.getUniformName(), loc);
        }
        setUniformLocation(loc);
    }


    public void close() {
        if (this.uniformIntBuffer != null) {
            MemoryUtil.memFree(this.uniformIntBuffer);
        }

        if (this.uniformFloatBuffer != null) {
            MemoryUtil.memFree(this.uniformFloatBuffer);
        }

    }

    private void markDirty() {
        this.dirty = true;
        if (this.shaderManager != null) {
            this.shaderManager.markDirty();
        }

    }

    public void setUniformLocation(int uniformLocationIn) {
        this.uniformLocation = uniformLocationIn;
    }

    public String getUniformName() {
        return this.uniformName;
    }


    public void upload() {
        if (!this.dirty) {
            return;
        }
        this.dirty = false;
        if (isIntType(this.uniformType)) {
            this.uploadInt();
        } else {
            this.uploadFloat();
        }
    }

    private void uploadInt() {
        this.uniformIntBuffer.clear();
        switch (this.uniformType) {
            case int1:
                RenderSystem.glUniform1(this.uniformLocation, this.uniformIntBuffer);
                break;
            case int2:
            case vec2i:
                RenderSystem.glUniform2(this.uniformLocation, this.uniformIntBuffer);
                break;
            case int3:
            case vec3i:
                RenderSystem.glUniform3(this.uniformLocation, this.uniformIntBuffer);
                break;
            case int4:
            case vec4i:
                RenderSystem.glUniform4(this.uniformLocation, this.uniformIntBuffer);
                break;
            default:
                HollowCore.LOGGER.warn("No method for uploading for {}", this.getUniformName());
        }

    }

    public void set(int... i) {
        this.uniformIntBuffer.position(0);
        int count = getCountForType(uniformType);
        if (i.length % count == 0) {
            this.uniformIntBuffer.put(i);
            this.markDirty();
        } else {
            HollowCore.LOGGER.warn(
                    "Trying to set a uniform expecting either {} or " +
                            "a multiple of {} values, provided {} values",
                    count, count, i.length);
        }
    }

    public void set(float... i) {
        this.uniformFloatBuffer.position(0);
        int count = getCountForType(uniformType);
        if (i.length % count == 0) {
            this.uniformFloatBuffer.put(i);
            this.markDirty();
        } else {
            HollowCore.LOGGER.warn(
                    "Trying to set uniform {} expecting either {} or " +
                            "a multiple of {} values, provided {} values",
                    this.getUniformName(), count, count, i.length);
        }
    }

    public void set(Matrix4f mat) {
        if (uniformType == UniformType.mat4x4) {
            this.uniformFloatBuffer.position(0);
            mat.store(this.uniformFloatBuffer);
            matCount = 1;
            this.markDirty();
        } else {
            HollowCore.LOGGER.warn(
                    "Trying to upload non 4x4 matrix to 4x4 mat uniform {}",
                    this.getUniformName());
        }
    }

    public void set(int count, Matrix4d... mats) {
        if (uniformType == UniformType.vecmat4x4) {
            this.uniformFloatBuffer.position(0);
            for (int c = 0; c < count; c++) {
                int offset = 16 * c;
                mats[c].get(offset, this.uniformFloatBuffer);
            }
            matCount = count;
            this.markDirty();
        } else {
            HollowCore.LOGGER.warn(
                    "Trying to upload mat4x4 vector to non vecmat4x4 type uniform {}",
                    this.getUniformName());
        }
    }

    public void set(Matrix4d... mats) {
        if (uniformType == UniformType.vecmat4x4) {
            int count = 0;
            this.uniformFloatBuffer.position(0);
            for (Matrix4d mat : mats) {
                int offset = 16 * count;
                mat.get(offset, this.uniformFloatBuffer);
                count++;
            }
            matCount = count;
            this.markDirty();
        } else {
            HollowCore.LOGGER.warn(
                    "Trying to upload mat4x4 vector to non vecmat4x4 type uniform {}",
                    this.getUniformName());
        }
    }

    public void set(Matrix4f... mats) {
        if (uniformType == UniformType.vecmat4x4) {
            int count = 0;
            this.uniformFloatBuffer.position(0);
            for (Matrix4f mat : mats) {
                int offset = 16 * count;
                storeMC(mat, offset, this.uniformFloatBuffer);
                count++;
            }
            matCount = count;
            this.markDirty();
        } else {
            HollowCore.LOGGER.warn(
                    "Trying to upload mat4x4 vector to non vecmat4x4 type uniform {}",
                    this.getUniformName());
        }
    }

    private void storeMC(Matrix4f mat, int offset, FloatBuffer uniformFloatBuffer) {
        uniformFloatBuffer.put(offset, mat.m00);
        uniformFloatBuffer.put(offset + 1, mat.m01);
        uniformFloatBuffer.put(offset + 2, mat.m02);
        uniformFloatBuffer.put(offset + 3, mat.m03);
        uniformFloatBuffer.put(offset + 4, mat.m10);
        uniformFloatBuffer.put(offset + 5, mat.m11);
        uniformFloatBuffer.put(offset + 6, mat.m12);
        uniformFloatBuffer.put(offset + 7, mat.m13);
        uniformFloatBuffer.put(offset + 8, mat.m20);
        uniformFloatBuffer.put(offset + 9, mat.m21);
        uniformFloatBuffer.put(offset + 10, mat.m22);
        uniformFloatBuffer.put(offset + 11, mat.m23);
        uniformFloatBuffer.put(offset + 12, mat.m30);
        uniformFloatBuffer.put(offset + 13, mat.m31);
        uniformFloatBuffer.put(offset + 14, mat.m32);
        uniformFloatBuffer.put(offset + 15, mat.m33);
    }


    private void uploadFloat() {
        this.uniformFloatBuffer.clear();
        switch (this.uniformType) {
            case float1:
                RenderSystem.glUniform1(this.uniformLocation, this.uniformFloatBuffer);
                break;
            case float2:
            case vec2f:
                RenderSystem.glUniform2(this.uniformLocation, this.uniformFloatBuffer);
                break;
            case float3:
            case vec3f:
                RenderSystem.glUniform3(this.uniformLocation, this.uniformFloatBuffer);
                break;
            case float4:
            case vec4f:
                RenderSystem.glUniform4(this.uniformLocation, this.uniformFloatBuffer);
                break;
            case mat2x2:
                RenderSystem.glUniformMatrix2(this.uniformLocation, false, this.uniformFloatBuffer);
                break;
            case mat3x3:
                RenderSystem.glUniformMatrix3(this.uniformLocation, false, this.uniformFloatBuffer);
                break;
            case vecmat4x4:
            case mat4x4:
                RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
                GL20C.nglUniformMatrix4fv(this.uniformLocation, (matCount * 16) >> 4, false, memAddress(this.uniformFloatBuffer));
                break;
            default:
                HollowCore.LOGGER.warn("No method for uploading for {}", this.getUniformName());
        }
    }
}