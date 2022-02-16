package ru.hollowhorizon.hc.client.render.game;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

@OnlyIn(Dist.CLIENT)
public final class VBO {
    private final int vboId;
    private final int type;

    VBO(final int vboId, final int type) {
        this.vboId = vboId;
        this.type = type;
    }

    public void bind() {
        GL15.glBindBuffer(type, vboId);
    }

    public void unbind() {
        GL15.glBindBuffer(type, 0);
    }

    public void storeData(final float[] data) {
        final FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        storeData(buffer);
    }

    public void storeData(final int[] data) {
        final IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
        buffer.put(data);
        buffer.flip();
        storeData(buffer);
    }


    public void storeData(final IntBuffer data) {
        GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW);
    }

    public void storeData(final FloatBuffer data) {
        GL15.glBufferData(type, data, GL15.GL_STATIC_DRAW);
    }

    @Override
    public String toString() {
        return "VBO{" +
                "vboId=" + vboId +
                ", type=" + type +
                '}';
    }
}
