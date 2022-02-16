package ru.hollowhorizon.hc.client.render.shader.uniforms;

import org.lwjgl.opengl.GL20;
import ru.hollowhorizon.hc.client.render.shader.Uniform;

import java.util.function.BiConsumer;

public class UniformPrimitive<T> extends Uniform<T> {

    private T currentBool;
    private boolean used = false;

    private final BiConsumer<Integer, T> openGlUploader;

    public UniformPrimitive(final String name, final BiConsumer<Integer, T> openGlUploader){
        super(name);
        this.openGlUploader = openGlUploader;
    }

    public void load(final T toLoad){
        if(!used || currentBool != toLoad){
            this.openGlUploader.accept(super.getLocation(), toLoad);

            used = true;
            currentBool = toLoad;
        }
    }

    public static class Bool extends UniformPrimitive<Boolean> {

        public Bool(final String name) {
            super(name,
                    (location, value) -> GL20.glUniform1f(location, value ? 1f : 0f)
            );
        }
    }

    public static class Int extends UniformPrimitive<Integer> {

        public Int(final String name) {
            super(name,
                    GL20::glUniform1i
            );
        }
    }

    public static class Float extends UniformPrimitive<java.lang.Float> {

        public Float(final String name) {
            super(name,
                    GL20::glUniform1f
            );
        }
    }

    public static class Sampler extends Int {

        public Sampler(final String name) {
            super(name);
        }
    }
}
