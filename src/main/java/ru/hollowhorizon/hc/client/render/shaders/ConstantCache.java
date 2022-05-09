package ru.hollowhorizon.hc.client.render.shaders;

/**
 * Provides methods to mutate specilization constants
 * before the shader is specialized.
 * <p>
 * Note that in contrast to {@link UniformCache}, the methods
 * contained within this class are <b>not</b> named after their
 * OpenGL counterparts.
 * <p>
 * Created by KitsuneAlex on 19/11/21.
 */
public interface ConstantCache {

    void constant1i(int id, int value);

    void constant1f(int id, float value);

    void constant1b(int id, boolean value);

}
