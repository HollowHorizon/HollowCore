package ru.hollowhorizon.hc.client.render.shader;

import com.google.common.collect.ImmutableList;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL40;
import org.lwjgl.opengl.GL43;
import ru.hollowhorizon.hc.client.render.OpenGLUtils;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public interface ShaderObject {

    String getName();

    ShaderType getShaderType();


    ImmutableList<Uniform> getUniforms();

    boolean isDirty();

    void alloc();

    int getShaderID();

    void onLink(int programId);

    interface ShaderType {

        int getGLCode();

        boolean isSupported();
    }

    enum StandardShaderType implements ShaderType {
        //@formatter:off
        VERTEX      (GL20.GL_VERTEX_SHADER,          () -> OpenGLUtils.openGL20),
        FRAGMENT    (GL20.GL_FRAGMENT_SHADER,        () -> OpenGLUtils.openGL20),
        GEOMETRY    (GL32.GL_GEOMETRY_SHADER,        () -> OpenGLUtils.openGL32),
        TESS_CONTROL(GL40.GL_TESS_CONTROL_SHADER,    () -> OpenGLUtils.openGL40),
        TESS_EVAL   (GL40.GL_TESS_EVALUATION_SHADER, () -> OpenGLUtils.openGL40),
        COMPUTE     (GL43.GL_COMPUTE_SHADER,         () -> OpenGLUtils.openGL43);
        //@formatter:on

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
}