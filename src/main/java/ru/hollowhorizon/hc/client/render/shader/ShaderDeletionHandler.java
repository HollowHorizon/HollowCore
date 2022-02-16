package ru.hollowhorizon.hc.client.render.shader;

import org.lwjgl.opengl.GL20;

final class ShaderDeletionHandler implements Runnable {

    private final int vertexShaderId;
    private final int fragmentShaderId;

    private final int programId;

    ShaderDeletionHandler(final int vertexShaderId, final int fragmentShaderId, final int programId) {
        this.vertexShaderId = vertexShaderId;
        this.fragmentShaderId = fragmentShaderId;
        this.programId = programId;
    }

    @Override
    public void run() {
        if (vertexShaderId != -1)
        {
            GL20.glDetachShader(programId, vertexShaderId);
            GL20.glDeleteShader(vertexShaderId);
        }

        if (fragmentShaderId != -1)
        {
            GL20.glDetachShader(programId, fragmentShaderId);
            GL20.glDeleteShader(fragmentShaderId);
        }

        GL20.glDeleteProgram(programId);
    }

    public int getProgramId() {
        return programId;
    }
}
