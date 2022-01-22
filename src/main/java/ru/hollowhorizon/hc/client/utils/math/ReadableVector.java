package ru.hollowhorizon.hc.client.utils.math;

import java.nio.FloatBuffer;

public interface ReadableVector {

    float length();

    float lengthSquared();

    Vector store(FloatBuffer buf);
}