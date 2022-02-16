package ru.hollowhorizon.hc.client.render.shader.uniforms;

import org.lwjgl.opengl.GL20;
import ru.hollowhorizon.hc.client.utils.math.Matrix;
import ru.hollowhorizon.hc.client.utils.math.Matrix2f;
import ru.hollowhorizon.hc.client.utils.math.Matrix3f;
import ru.hollowhorizon.hc.client.utils.math.Matrix4f;

import java.nio.FloatBuffer;
import java.util.function.BiConsumer;

public class UniformMatrix<M extends Matrix<M>> extends FloatBufferWritingUniform<M> {

    public UniformMatrix(final String name, final int dimension, final BiConsumer<Integer, FloatBuffer> openGlUploader) {
        super(name, dimension*dimension , openGlUploader);
    }

    public static class Mat2 extends UniformMatrix<Matrix2f> {

        public Mat2(final String name) {
            super(name,
                    2,
                    (location, buffer) -> GL20.glUniformMatrix2fv(location, false, buffer));
        }
    }

    public static class Mat3 extends UniformMatrix<Matrix3f> {

        public Mat3(final String name) {
            super(name,
                    3,
                    (location, buffer) -> GL20.glUniformMatrix3fv(location, false, buffer));
        }
    }

    public static class Mat4 extends UniformMatrix<Matrix4f> {

        public Mat4(final String name) {
            super(name,
                    4,
                    (location, buffer) -> GL20.glUniformMatrix4fv(location, false, buffer));
        }

        public void load(final net.minecraft.util.math.vector.Matrix4f toLoad) {
            super.loadWithBuffer(toLoad::store);
        }
    }

}
