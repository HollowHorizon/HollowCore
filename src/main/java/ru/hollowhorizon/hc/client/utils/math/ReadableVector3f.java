package ru.hollowhorizon.hc.client.utils.math;

public interface ReadableVector3f<R extends ReadableVector3f<R>> extends ReadableVector2f<R> {
    /**
     * @return z
     */
    float getZ();
}
