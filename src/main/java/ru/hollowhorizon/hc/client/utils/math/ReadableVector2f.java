package ru.hollowhorizon.hc.client.utils.math;

public interface ReadableVector2f<R extends ReadableVector2f<R>> extends ReadableVector<R> {
    /**
     * @return x
     */
    float getX();
    /**
     * @return y
     */
    float getY();
}
