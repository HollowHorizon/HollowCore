package ru.hollowhorizon.hc.client.utils.math;

import ru.hollowhorizon.hc.client.utils.WriteableToFloatBuffer;

import java.io.Serializable;
import java.nio.FloatBuffer;

public abstract class Matrix<M extends Matrix<M>> implements Serializable, WriteableToFloatBuffer<M> {

    protected Matrix() {
        super();
    }

    public abstract M setIdentity();

    public abstract M invert();

    public abstract M load(FloatBuffer buf);

    public abstract M loadTranspose(FloatBuffer buf);

    public abstract M negate();

    public abstract M storeTranspose(FloatBuffer buf);

    public abstract M transpose();

    public abstract M setZero();

    public abstract float determinant();
}
