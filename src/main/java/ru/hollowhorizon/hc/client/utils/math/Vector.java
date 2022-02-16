package ru.hollowhorizon.hc.client.utils.math;

import java.io.Serializable;
import java.nio.FloatBuffer;

public abstract class Vector<V extends Vector<V>> implements Serializable, ReadableVector<V> {

    /**
     * Constructor for Vector.
     */
    protected Vector() {
        super();
    }

    /**
     * @return the length of the vector
     */
    public final float length() {
        return (float) Math.sqrt(lengthSquared());
    }


    /**
     * @return the length squared of the vector
     */
    public abstract float lengthSquared();

    /**
     * Load this vector from a FloatBuffer
     * @param buf The buffer to load it from, at the current position
     * @return this
     */
    public abstract V load(FloatBuffer buf);

    /**
     * Negate a vector
     * @return this
     */
    public abstract V negate();


    /**
     * Normalise this vector
     * @return this
     */
    public final V normalise() {
        float len = length();
        if (len != 0.0f) {
            float l = 1.0f / len;
            return scale(l);
        } else
            throw new IllegalStateException("Zero length vector");
    }


    /**
     * Store this vector in a FloatBuffer
     * @param buf The buffer to store it in, at the current position
     * @return this
     */
    public abstract V store(FloatBuffer buf);


    /**
     * Scale this vector
     * @param scale The scale factor
     * @return this
     */
    public abstract V scale(float scale);



}
