package ru.hollowhorizon.hc.client.utils.math;

public interface ReadableVector4f<R extends ReadableVector4f<R>> extends ReadableVector3f<R> {

    /**
     * @return w
     */
    float getW();

}