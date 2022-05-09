package ru.hollowhorizon.hc.client.utils;

import java.nio.FloatBuffer;

public interface WriteableToFloatBuffer<T> {

    T store(FloatBuffer floatBuffer);
}
