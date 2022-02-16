package ru.hollowhorizon.hc.client.utils;

import java.nio.FloatBuffer;

public interface WriteableToFloatBuffer<T> {

    /**
     * Method used to write this object into a float buffer.
     *
     * @param floatBuffer The buffer to write the data into.
     * @return The object on which this method was invoked. {@code this}.
     */
    T store(FloatBuffer floatBuffer);
}
