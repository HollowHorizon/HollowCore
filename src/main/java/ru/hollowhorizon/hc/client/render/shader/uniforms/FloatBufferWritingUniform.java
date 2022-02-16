package ru.hollowhorizon.hc.client.render.shader.uniforms;

import org.lwjgl.BufferUtils;
import ru.hollowhorizon.hc.client.render.shader.Uniform;
import ru.hollowhorizon.hc.client.utils.WriteableToFloatBuffer;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

class FloatBufferWritingUniform<T extends WriteableToFloatBuffer<T>> extends Uniform<T> {
    protected final FloatBuffer dataBuffer;
    protected final BiConsumer<Integer, FloatBuffer> openGlUploader;

    FloatBufferWritingUniform(final String name, final int dimension, final BiConsumer<Integer, FloatBuffer> openGlUploader) {
        super(name);
        dataBuffer = BufferUtils.createFloatBuffer(dimension);
        this.openGlUploader = openGlUploader;
    }

    public void load(final T toLoad) {
        this.loadWithBuffer(toLoad::store);
    }

    protected void loadWithBuffer(Consumer<FloatBuffer> bufferWriter) {
        dataBuffer.position(0);
        bufferWriter.accept(dataBuffer);
        dataBuffer.clear();
        this.openGlUploader.accept(getLocation(), dataBuffer);
    }
}
