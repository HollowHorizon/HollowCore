package ru.hollowhorizon.hc.client.render.shaders;

import org.lwjgl.opengl.GL46;

public enum BinaryType {
    SPIR_V(GL46.GL_SHADER_BINARY_FORMAT_SPIR_V);

    private final int glCode;

    BinaryType(int glCode) {
        this.glCode = glCode;
    }

    public int getGLCode() {
        return glCode;
    }
}
