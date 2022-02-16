package ru.hollowhorizon.hc.client.utils.math;

import ru.hollowhorizon.hc.client.utils.WriteableToFloatBuffer;

import java.nio.FloatBuffer;

public interface ReadableVector<R extends ReadableVector<R>> extends WriteableToFloatBuffer<R> {
    /**
     * @return the length of the vector
     */
    float length();
    /**
     * @return the length squared of the vector
     */
    float lengthSquared();
}