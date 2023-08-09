package ru.hollowhorizon.hc.client.render.shaders;

import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL21;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL40;
import ru.hollowhorizon.hc.client.utils.math.MatrixUtils;

import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ShaderUniformCache implements UniformCache {

    private final ShaderProgram program;

    private final Map<String, UniformEntry<?>> uniformEntries = new HashMap<>();
    private final Object2IntMap<String> locationCache = new Object2IntOpenHashMap<>();

    public ShaderUniformCache(ShaderProgram program) {
        this.program = program;
        for (Uniform uniform : program.getUniforms()) {
            uniformEntries.put(uniform.name(), makeEntry(uniform));
        }
    }

    private static double[] toArrayD(Matrix3f matrix) {
        var buffer = FloatBuffer.allocate(9);
        matrix.store(buffer);
        var array = buffer.array();

        return new double[]{
                array[0],
                array[1],
                array[2],
                array[3],
                array[4],
                array[5],
                array[6],
                array[7],
                array[8]
        };
    }

    private static float[] toArrayF(Matrix3f matrix) {
        var buffer = FloatBuffer.allocate(9);
        matrix.store(buffer);
        return buffer.array();
    }

    public void onLink() {
        locationCache.clear();
        for (UniformEntry<?> entry : uniformEntries.values()) {
            entry.reset();
            Uniform uniform = entry.uniform;
            locationCache.put(uniform.name(), GL20.glGetUniformLocation(program.getProgramId(), uniform.name()));
        }
    }

    public void use() {
        for (UniformEntry<?> entry : uniformEntries.values()) {
            entry.apply();
        }
    }

    private UniformEntry<?> makeEntry(Uniform uniform) {
        switch (uniform.type().getCarrier()) {
            case INT:
            case U_INT:
                return new IntUniformEntry(uniform);
            case FLOAT:
            case MATRIX:
                return new FloatUniformEntry(uniform);
            case DOUBLE:
            case D_MATRIX:
                return new DoubleUniformEntry(uniform);
        }
        return null;
    }


    @Override
    public void glUniform1i(String name, int i0) {
        glUniformI(name, i0);
    }

    @Override
    public void glUniform2i(String name, int i0, int i1) {
        glUniformI(name, i0, i1);
    }

    @Override
    public void glUniform3i(String name, int i0, int i1, int i2) {
        glUniformI(name, i0, i1, i2);
    }

    @Override
    public void glUniform4i(String name, int i0, int i1, int i2, int i3) {
        glUniformI(name, i0, i1, i2, i3);
    }

    @Override
    public void glUniform1ui(String name, int i0) {
        glUniformI(name, i0);
    }

    @Override
    public void glUniform2ui(String name, int i0, int i1) {
        glUniformI(name, i0, i1);
    }

    @Override
    public void glUniform3ui(String name, int i0, int i1, int i2) {
        glUniformI(name, i0, i1, i2);
    }

    @Override
    public void glUniform4ui(String name, int i0, int i1, int i2, int i3) {
        glUniformI(name, i0, i1, i2, i3);
    }

    @Override
    public void glUniform1f(String name, float f0) {
        glUniformF(name, false, f0);
    }

    @Override
    public void glUniform2f(String name, float f0, float f1) {
        glUniformF(name, false, f0, f1);
    }

    @Override
    public void glUniform3f(String name, float f0, float f1, float f2) {
        glUniformF(name, false, f0, f1, f2);
    }

    @Override
    public void glUniform4f(String name, float f0, float f1, float f2, float f3) {
        glUniformF(name, false, f0, f1, f2, f3);
    }

    @Override
    public void glUniform1d(String name, float d0) {
        glUniformD(name, false, d0);
    }

    @Override
    public void glUniform2d(String name, float d0, float d1) {
        glUniformD(name, false, d0, d1);
    }

    @Override
    public void glUniform3d(String name, float d0, float d1, float d2) {
        glUniformD(name, false, d0, d1, d2);
    }

    @Override
    public void glUniform4d(String name, float d0, float d1, float d2, float d3) {
        glUniformD(name, false, d0, d1, d2, d3);
    }

    @Override
    public void glUniform1b(String name, boolean b0) {
        glUniform1i(name, b0 ? 1 : 0);
    }

    @Override
    public void glUniform2b(String name, boolean b0, boolean b1) {
        glUniform2i(name, b0 ? 1 : 0, b1 ? 1 : 0);
    }

    @Override
    public void glUniform3b(String name, boolean b0, boolean b1, boolean b2) {
        glUniform3i(name, b0 ? 1 : 0, b1 ? 1 : 0, b2 ? 1 : 0);
    }

    @Override
    public void glUniform4b(String name, boolean b0, boolean b1, boolean b2, boolean b3) {
        glUniform4i(name, b0 ? 1 : 0, b1 ? 1 : 0, b2 ? 1 : 0, b3 ? 1 : 0);
    }

    @Override
    public void glUniformMatrix2f(String name, float[] matrix) {
        glUniformMatrix2f(name, false, matrix);
    }

    @Override
    public void glUniformMatrix2f(String name, boolean transpose, float[] matrix) {
        glUniformF(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix2x3f(String name, float[] matrix) {
        glUniformMatrix2x3f(name, false, matrix);
    }

    @Override
    public void glUniformMatrix2x3f(String name, boolean transpose, float[] matrix) {
        glUniformF(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix2x4f(String name, float[] matrix) {
        glUniformMatrix2x4f(name, false, matrix);
    }

    @Override
    public void glUniformMatrix2x4f(String name, boolean transpose, float[] matrix) {
        glUniformF(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix3f(String name, float[] matrix) {
        glUniformMatrix3f(name, false, matrix);
    }

    @Override
    public void glUniformMatrix3f(String name, boolean transpose, float[] matrix) {
        glUniformF(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix3f(String name, Matrix3f matrix) {
        glUniformMatrix3f(name, false, matrix);
    }

    @Override
    public void glUniformMatrix3f(String name, boolean transpose, Matrix3f matrix) {
        glUniformF(name, transpose, toArrayF(matrix));
    }

    @Override
    public void glUniformMatrix3x2f(String name, float[] matrix) {
        glUniformMatrix3x2f(name, false, matrix);
    }

    @Override
    public void glUniformMatrix3x2f(String name, boolean transpose, float[] matrix) {
        glUniformF(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix3x4f(String name, float[] matrix) {
        glUniformMatrix3x4f(name, false, matrix);
    }

    @Override
    public void glUniformMatrix3x4f(String name, boolean transpose, float[] matrix) {
        glUniformF(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix4f(String name, float[] matrix) {
        glUniformMatrix4f(name, false, matrix);
    }

    @Override
    public void glUniformMatrix4f(String name, boolean transpose, float[] matrix) {
        glUniformF(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix4f(String name, Matrix4f matrix) {
        glUniformMatrix4f(name, MatrixUtils.getMatrix(matrix));
    }

    @Override
    public void glUniformArrayMatrix4f(String name, int size, boolean transpose, float[] matrix) {
        for(int i = 0; i < size; i++) {
            glUniformMatrix4f(name+"["+i+"]", transpose, matrix);
        }
    }

    @Override
    public void glUniformArrayMatrix4f(String name, int size, Matrix4f matrix4f) {
        for(int i = 0; i < size; i++) {
            glUniformMatrix4f(name+"["+i+"]", false, matrix4f);
        }
    }

    @Override
    public void glUniformMatrix4f(String name, boolean transpose, Matrix4f matrix) {
        glUniformMatrix4f(name, transpose, MatrixUtils.getMatrix(matrix));
    }

    @Override
    public void glUniformMatrix4x2f(String name, float[] matrix) {
        glUniformMatrix4x2f(name, false, matrix);
    }

    @Override
    public void glUniformMatrix4x2f(String name, boolean transpose, float[] matrix) {
        glUniformF(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix4x3f(String name, float[] matrix) {
        glUniformMatrix4x3f(name, false, matrix);
    }

    @Override
    public void glUniformMatrix4x3f(String name, boolean transpose, float[] matrix) {
        glUniformF(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix2d(String name, double[] matrix) {
        glUniformMatrix2d(name, false, matrix);
    }

    @Override
    public void glUniformMatrix2d(String name, boolean transpose, double[] matrix) {
        glUniformD(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix2x3d(String name, double[] matrix) {
        glUniformMatrix2x3d(name, false, matrix);
    }

    @Override
    public void glUniformMatrix2x3d(String name, boolean transpose, double[] matrix) {
        glUniformD(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix2x4d(String name, double[] matrix) {
        glUniformMatrix2x4d(name, false, matrix);
    }

    @Override
    public void glUniformMatrix2x4d(String name, boolean transpose, double[] matrix) {
        glUniformD(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix3d(String name, double[] matrix) {
        glUniformMatrix3d(name, false, matrix);
    }

    @Override
    public void glUniformMatrix3d(String name, boolean transpose, double[] matrix) {
        glUniformD(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix3d(String name, Matrix3f matrix) {
        glUniformMatrix3d(name, false, matrix);
    }

    @Override
    public void glUniformMatrix3d(String name, boolean transpose, Matrix3f matrix) {
        glUniformD(name, transpose, toArrayD(matrix));
    }

    @Override
    public void glUniformMatrix3x2d(String name, double[] matrix) {
        glUniformMatrix3x2d(name, false, matrix);
    }

    @Override
    public void glUniformMatrix3x2d(String name, boolean transpose, double[] matrix) {
        glUniformD(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix3x4d(String name, double[] matrix) {
        glUniformMatrix3x4d(name, false, matrix);
    }

    @Override
    public void glUniformMatrix3x4d(String name, boolean transpose, double[] matrix) {
        glUniformD(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix4d(String name, double[] matrix) {
        glUniformMatrix4d(name, false, matrix);
    }

    @Override
    public void glUniformMatrix4d(String name, boolean transpose, double[] matrix) {
        glUniformD(name, transpose, matrix);
    }

    @Override
    public void glUniformMatrix4d(String name, Matrix4f matrix) {

    }

    @Override
    public void glUniformMatrix4d(String name, boolean transpose, Matrix4f matrix) {

    }

    @Override
    public void glUniformMatrix4x2d(String name, double[] matrix) {
        glUniformMatrix4x2d(name, false, matrix);
    }

    @Override
    public void glUniformMatrix4x2d(String name, boolean transpose, double[] matrix) {
        glUniformD(name, transpose, matrix);
    }


    @Override
    public void glUniformMatrix4x3d(String name, double[] matrix) {
        glUniformMatrix4x3d(name, false, matrix);
    }

    @Override
    public void glUniformMatrix4x3d(String name, boolean transpose, double[] matrix) {
        glUniformD(name, transpose, matrix);
    }

    private void glUniformI(String name, int... values) {
        UniformEntry<?> entry = uniformEntries.get(name);
        if (entry == null) {
            throw new IllegalArgumentException(String.format("Uniform with name '%s' does not exist.", name));
        }
        UniformType type = entry.uniform.type();
        if (!(entry instanceof IntUniformEntry)) {
            throw new IllegalArgumentException(String.format("Uniform with name '%s' isn't registered with the carrier of INT, Got type '%s' with carrier '%s'.", name, type, type.getCarrier()));
        }
        if (type.getSize() != values.length) {
            throw new IllegalArgumentException(String.format("Invalid uniform length, Expected: '%s', Got: '%s'.", type.getSize(), values.length));
        }

        ((IntUniformEntry) entry).set(values, false);
    }

    private void glUniformF(String name, boolean transpose, float... values) {
        UniformEntry<?> entry = uniformEntries.get(name);
        if (entry == null) {
            throw new IllegalArgumentException(String.format("Uniform with name '%s' does not exist.", name));
        }
        UniformType type = entry.uniform.type();
        if (!(entry instanceof FloatUniformEntry)) {
            throw new IllegalArgumentException(String.format("Uniform with name '%s' isn't registered with the carrier of FLOAT or MATRIX, Got type '%s' with carrier '%s'.", name, type, type.getCarrier()));
        }
        if (type.getSize() != values.length) {
            throw new IllegalArgumentException(String.format("Invalid uniform length, Expected: '%s', Got: '%s'.", type.getSize(), values.length));
        }

        ((FloatUniformEntry) entry).set(values, transpose);
    }

    private void glUniformD(String name, boolean transpose, double... values) {
        UniformEntry<?> entry = uniformEntries.get(name);
        if (entry == null) {
            throw new IllegalArgumentException(String.format("Uniform with name '%s' does not exist.", name));
        }
        UniformType type = entry.uniform.type();
        if (!(entry instanceof DoubleUniformEntry)) {
            throw new IllegalArgumentException(String.format("Uniform with name '%s' isn't registered with the carrier of DOUBLE or D_MATRIX, Got type '%s' with carrier '%s'.", name, type, type.getCarrier()));
        }
        if (type.getSize() != values.length) {
            throw new IllegalArgumentException(String.format("Invalid uniform length, Expected: '%s', Got: '%s'.", type.getSize(), values.length));
        }

        ((DoubleUniformEntry) entry).set(values, transpose);
    }

    public abstract class UniformEntry<T> {

        protected final Uniform uniform;
        protected final UniformType type;
        protected T cache;
        protected boolean transpose;
        protected boolean dirty;
        private int location = -1;

        protected UniformEntry(Uniform uniform) {
            this.uniform = uniform;
            type = uniform.type();
            reset();
        }

        public void set(UniformEntry<T> other) {
            set(other.cache, other.transpose);
        }

        public void set(T values, boolean transpose) {
            assert !transpose || type.getCarrier() == UniformType.Carrier.MATRIX || type.getCarrier() == UniformType.Carrier.D_MATRIX;

            if (len(values) != type.getSize()) {
                throw new IllegalArgumentException(String.format("Invalid size for uniform '%s', Expected: '%s', Got: '%s'.", uniform.name(), type.getSize(), len(values)));
            }
            if (!equals(cache, values) || this.transpose != transpose) {
                cache = values;
                this.transpose = transpose;
                dirty = true;
            }
        }

        public int getLocation() {
            if (location == -1) {
                location = locationCache.getInt(uniform.name());
            }
            return location;
        }

        public void reset() {
            cache = make(type.getSize());
            location = -1;
        }

        public abstract void apply();

        public abstract T make(int len);

        public abstract int len(T cache);

        public abstract T clone(T other);

        public abstract boolean equals(T a, T b);
    }

    public class IntUniformEntry extends UniformEntry<int[]> {

        public IntUniformEntry(Uniform uniform) {
            super(uniform);
        }

        @Override
        public void apply() {
            if (!dirty) return;

            switch (type.getCarrier()) {

                case INT:
                    switch (type.getSize()) {
                        case 1:
                            GL20.glUniform1i(getLocation(), cache[0]);
                            break;
                        case 2:
                            GL20.glUniform2i(getLocation(), cache[0], cache[1]);
                            break;
                        case 3:
                            GL20.glUniform3i(getLocation(), cache[0], cache[1], cache[2]);
                            break;
                        case 4:
                            GL20.glUniform4i(getLocation(), cache[0], cache[1], cache[2], cache[3]);
                            break;
                        default:
                            throw new IllegalStateException("Invalid size for Int type." + type.getSize());
                    }
                    break;
                case U_INT:
                    switch (type.getSize()) {
                        case 1:
                            GL30.glUniform1ui(getLocation(), cache[0]);
                            break;
                        case 2:
                            GL30.glUniform2ui(getLocation(), cache[0], cache[1]);
                            break;
                        case 3:
                            GL30.glUniform3ui(getLocation(), cache[0], cache[1], cache[2]);
                            break;
                        case 4:
                            GL30.glUniform4ui(getLocation(), cache[0], cache[1], cache[2], cache[3]);
                            break;
                        default:
                            throw new IllegalStateException("Invalid size for Int type." + type.getSize());
                    }
                    break;
                default:
                    throw new IllegalStateException("Invalid type for IntUniformEntry: " + type.getCarrier());

            }
            dirty = false;
        }


        @Override
        public int[] make(int len) {
            return new int[len];
        }

        @Override
        public int len(int[] cache) {
            return cache.length;
        }

        @Override
        public int[] clone(int[] other) {
            return other.clone();
        }

        @Override
        public boolean equals(int[] a, int[] b) {
            return Arrays.equals(a, b);
        }

    }

    private class FloatUniformEntry extends UniformEntry<float[]> {

        public FloatUniformEntry(Uniform uniform) {
            super(uniform);
        }

        @Override
        public void apply() {
            if (!dirty) return;

            switch (type.getCarrier()) {

                case FLOAT:
                    switch (type.getSize()) {
                        case 1:
                            GL20.glUniform1f(getLocation(), cache[0]);
                            break;
                        case 2:
                            GL20.glUniform2f(getLocation(), cache[0], cache[1]);
                            break;
                        case 3:
                            GL20.glUniform3f(getLocation(), cache[0], cache[1], cache[2]);
                            break;
                        case 4:
                            GL20.glUniform4f(getLocation(), cache[0], cache[1], cache[2], cache[3]);
                            break;
                        default:
                            throw new IllegalStateException("Invalid size for Float type." + type.getSize());
                    }
                    break;
                case MATRIX:
                    switch (type) {
                        case MAT2:
                            GL20.glUniformMatrix2fv(getLocation(), transpose, cache);
                            break;
                        case MAT3:
                            GL20.glUniformMatrix3fv(getLocation(), transpose, cache);
                            break;
                        case MAT4:
                            GL20.glUniformMatrix4fv(getLocation(), transpose, cache);
                            break;
                        case MAT2x3:
                            GL21.glUniformMatrix2x3fv(getLocation(), transpose, cache);
                            break;
                        case MAT2x4:
                            GL21.glUniformMatrix2x4fv(getLocation(), transpose, cache);
                            break;
                        case MAT3x2:
                            GL21.glUniformMatrix3x2fv(getLocation(), transpose, cache);
                            break;
                        case MAT3x4:
                            GL21.glUniformMatrix3x4fv(getLocation(), transpose, cache);
                            break;
                        case MAT4x2:
                            GL21.glUniformMatrix4x2fv(getLocation(), transpose, cache);
                            break;
                        case MAT4x3:
                            GL21.glUniformMatrix4x3fv(getLocation(), transpose, cache);
                            break;
                        default:
                            throw new IllegalStateException("Invalid Matrix type: " + type);
                    }
                    break;
                default:
                    throw new IllegalStateException("Invalid type for FloatUniformEntry: " + type.getCarrier());

            }
            dirty = false;
        }


        @Override
        public float[] make(int len) {
            return new float[len];
        }

        @Override
        public int len(float[] cache) {
            return cache.length;
        }

        @Override
        public float[] clone(float[] other) {
            return other.clone();
        }

        @Override
        public boolean equals(float[] a, float[] b) {
            return Arrays.equals(a, b);
        }
    }

    private class DoubleUniformEntry extends UniformEntry<double[]> {

        public DoubleUniformEntry(Uniform uniform) {
            super(uniform);
        }

        @Override
        public void apply() {
            if (!dirty) return;

            switch (type.getCarrier()) {
                case DOUBLE:
                    switch (type.getSize()) {
                        case 1:
                            GL40.glUniform1d(getLocation(), cache[0]);
                            break;
                        case 2:
                            GL40.glUniform2d(getLocation(), cache[0], cache[1]);
                            break;
                        case 3:
                            GL40.glUniform3d(getLocation(), cache[0], cache[1], cache[2]);
                            break;
                        case 4:
                            GL40.glUniform4d(getLocation(), cache[0], cache[1], cache[2], cache[3]);
                            break;
                        default:
                            throw new IllegalStateException("Invalid size for Double type." + type.getSize());
                    }
                    break;
                case D_MATRIX:
                    switch (type) {
                        case D_MAT2:
                            GL40.glUniformMatrix2dv(getLocation(), transpose, cache);
                            break;
                        case D_MAT3:
                            GL40.glUniformMatrix3dv(getLocation(), transpose, cache);
                            break;
                        case D_MAT4:
                            GL40.glUniformMatrix4dv(getLocation(), transpose, cache);
                            break;
                        case D_MAT2x3:
                            GL40.glUniformMatrix2x3dv(getLocation(), transpose, cache);
                            break;
                        case D_MAT2x4:
                            GL40.glUniformMatrix2x4dv(getLocation(), transpose, cache);
                            break;
                        case D_MAT3x2:
                            GL40.glUniformMatrix3x2dv(getLocation(), transpose, cache);
                            break;
                        case D_MAT3x4:
                            GL40.glUniformMatrix3x4dv(getLocation(), transpose, cache);
                            break;
                        case D_MAT4x2:
                            GL40.glUniformMatrix4x2dv(getLocation(), transpose, cache);
                            break;
                        case D_MAT4x3:
                            GL40.glUniformMatrix4x3dv(getLocation(), transpose, cache);
                            break;
                        default:
                            throw new IllegalStateException("Invalid Matrix type: " + type);
                    }
                    break;
                default:
                    throw new IllegalStateException("Invalid type for DoubleUniformEntry: " + type.getCarrier());
            }
            dirty = false;
        }


        @Override
        public double[] make(int len) {
            return new double[len];
        }

        @Override
        public int len(double[] cache) {
            return cache.length;
        }

        @Override
        public double[] clone(double[] other) {
            return other.clone();
        }

        @Override
        public boolean equals(double[] a, double[] b) {
            return Arrays.equals(a, b);
        }

    }
}
