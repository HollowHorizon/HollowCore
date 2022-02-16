package ru.hollowhorizon.hc.client.render.game;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a OpenGL version 3.0 memory array on the GPU.
 */
@OnlyIn(Dist.CLIENT)
public final class VAO {
    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_INT = 4;
    private final int id;
    private final Collection<VBO> dataVBOs = new ArrayList<>();
    private int indexCount;

    VAO(final int id) {
        this.id = id;
    }

    public int getIndexCount() {
        return indexCount;
    }

    public void createIndexBuffer(final int[] indices) {
        VBO indexVbo = GPUMemoryManager.INSTANCE.createVBO(GL15.GL_ELEMENT_ARRAY_BUFFER);
        indexVbo.bind();
        indexVbo.storeData(indices);
        this.indexCount = indices.length;
    }

    public void createAttribute(final int attribute, final float[] data, final int attrSize) {
        final VBO dataVbo = GPUMemoryManager.INSTANCE.createVBO(GL15.GL_ARRAY_BUFFER);
        dataVbo.bind();
        dataVbo.storeData(data);
        GL20.glVertexAttribPointer(attribute, attrSize, GL11.GL_FLOAT, false, attrSize * BYTES_PER_FLOAT, 0);
        dataVbo.unbind();
        dataVBOs.add(dataVbo);
    }

    public void createIntAttribute(final int attribute, final int[] data, int attrSize) {
        final VBO dataVbo = GPUMemoryManager.INSTANCE.createVBO(GL15.GL_ARRAY_BUFFER);
        dataVbo.bind();
        dataVbo.storeData(data);
        GL30.glVertexAttribIPointer(attribute, attrSize, GL11.GL_INT, attrSize * BYTES_PER_INT, 0);
        dataVbo.unbind();
        dataVBOs.add(dataVbo);
    }

    public void bind(final int... attributes) {
        bind();
        for (final int i : attributes) {
            GL20.glEnableVertexAttribArray(i);
        }
    }

    public void unbind(final int... attributes) {
        for (final int i : attributes) {
            GL20.glDisableVertexAttribArray(i);
        }
        unbind();
    }

    public void bind() {
        GL30.glBindVertexArray(id);
    }

    public void unbind() {
        GL30.glBindVertexArray(0);
    }

    @Override
    public String toString() {
        return "VAO{" +
                "id=" + id +
                '}';
    }
}
