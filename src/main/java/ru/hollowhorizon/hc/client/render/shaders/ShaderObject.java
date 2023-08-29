package ru.hollowhorizon.hc.client.render.shaders;

import com.google.common.collect.ImmutableList;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public interface ShaderObject {
    String getShaderName();

    ShaderType getShaderType();

    ImmutableList<Uniform> getUniforms();

    boolean isDirty();

    void alloc();

    int getShaderID();

    void onLink(int programId);

    enum StandardShaderType implements ShaderType {
        VERTEX(GL20.GL_VERTEX_SHADER, () -> true),
        FRAGMENT(GL20.GL_FRAGMENT_SHADER, () -> true),
        GEOMETRY(GL32.GL_GEOMETRY_SHADER, () -> true),
        TESS_CONTROL(GL40.GL_TESS_CONTROL_SHADER, () -> true),
        TESS_EVAL(GL40.GL_TESS_EVALUATION_SHADER, () -> true),
        COMPUTE(GL43.GL_COMPUTE_SHADER, () -> true);

        private final int glCode;
        private BooleanSupplier func;
        private boolean isSupported;

        StandardShaderType(int glCode, BooleanSupplier func) {
            this.glCode = glCode;
            this.func = Objects.requireNonNull(func);
        }

        @Override
        public int getGLCode() {
            return glCode;
        }

        @Override
        public boolean isSupported() {
            if (func != null) {
                isSupported = func.getAsBoolean();
                func = null;
            }
            return isSupported;
        }
    }

    interface ShaderType {
        int getGLCode();

        boolean isSupported();
    }
}
