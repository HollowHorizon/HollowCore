package ru.hollowhorizon.hc.client.model.dae.shader.uniform;

import ru.hollowhorizon.hc.client.utils.math.*;

import java.util.function.Function;

public class UniformArray<T, U extends Uniform<T>> extends Uniform<T[]> {

    private final U[] arrayUniforms;

    public UniformArray(final String name, final int size, final Function<Integer, U[]> arrayConstructor, final Function<String, U> constructor) {
        super(name);
        arrayUniforms = arrayConstructor.apply(size);
        for(int i=0;i<size;i++){
            arrayUniforms[i] = constructor.apply(name + "["+i+"]");
        }
    }

    @Override
    public void storeUniformLocation(final int programID) {
        for(final U uniform : arrayUniforms){
            uniform.storeUniformLocation(programID);
        }
    }

    public void load(final T[] toLoad){
        for(int i=0;i<toLoad.length;i++){
            arrayUniforms[i].load(toLoad[i]);
        }
    }

    public static class Mat2 extends UniformArray<Matrix2f, UniformMatrix.Mat2> {

        public Mat2(final String name, final int size) {
            super(name,
                    size,
                    UniformMatrix.Mat2[]::new,
                    UniformMatrix.Mat2::new
            );
        }
    }

    public static class Mat3 extends UniformArray<Matrix3f, UniformMatrix.Mat3> {

        public Mat3(final String name, final int size) {
            super(name,
                    size,
                    UniformMatrix.Mat3[]::new,
                    UniformMatrix.Mat3::new
            );
        }
    }

    public static class Mat4 extends UniformArray<Matrix4f, UniformMatrix.Mat4> {

        public Mat4(final String name, final int size) {
            super(name,
                    size,
                    UniformMatrix.Mat4[]::new,
                    UniformMatrix.Mat4::new
            );
        }
    }

    public static class Vec2 extends UniformArray<Vector2f, UniformVector.Vec2> {

        public Vec2(final String name, final int size) {
            super(name,
                    size,
                    UniformVector.Vec2[]::new,
                    UniformVector.Vec2::new
            );
        }
    }

    public static class Vec3 extends UniformArray<Vector3f, UniformVector.Vec3> {

        public Vec3(final String name, final int size) {
            super(name,
                    size,
                    UniformVector.Vec3[]::new,
                    UniformVector.Vec3::new
            );
        }
    }

    public static class Vec4 extends UniformArray<Vector4f, UniformVector.Vec4> {

        public Vec4(final String name, final int size) {
            super(name,
                    size,
                    UniformVector.Vec4[]::new,
                    UniformVector.Vec4::new
            );
        }
    }

    public static class Bool extends UniformArray<Boolean, UniformPrimitive.Bool> {

        public Bool(final String name, final int size) {
            super(name,
                    size,
                    UniformPrimitive.Bool[]::new,
                    UniformPrimitive.Bool::new
            );
        }
    }

    public static class Int extends UniformArray<Integer, UniformPrimitive.Int> {

        public Int(final String name, final int size) {
            super(name,
                    size,
                    UniformPrimitive.Int[]::new,
                    UniformPrimitive.Int::new
            );
        }
    }

    public static class Float extends UniformArray<java.lang.Float, UniformPrimitive.Float> {

        public Float(final String name, final int size) {
            super(name,
                    size,
                    UniformPrimitive.Float[]::new,
                    UniformPrimitive.Float::new
            );
        }
    }
}
