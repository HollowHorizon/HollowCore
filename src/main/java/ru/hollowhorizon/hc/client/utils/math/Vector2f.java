package ru.hollowhorizon.hc.client.utils.math;

import java.io.Serializable;
import java.nio.FloatBuffer;

public class Vector2f extends Vector<Vector2f> implements Serializable, ReadableVector2f<Vector2f>, WritableVector2f {

    private static final long serialVersionUID = 1L;

    public float x, y;

    /**
     * Constructor for Vector2f.
     */
    public Vector2f() {
        super();
    }

    /**
     * Constructor.
     */
    public Vector2f(ReadableVector2f src) {
        set(src);
    }

    /**
     * Constructor.
     */
    public Vector2f(float x, float y) {
        set(x, y);
    }

    /* (non-Javadoc)
     * @see com.ldtteam.graphicsexpanded.util.math.WritableVector2f#set(float, float)
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Load from another Vector2f
     * @param src The source vector
     * @return this
     */
    public Vector2f set(ReadableVector2f src) {
        x = src.getX();
        y = src.getY();
        return this;
    }

    /**
     * @return the length squared of the vector
     */
    public float lengthSquared() {
        return x * x + y * y;
    }

    /**
     * Translate a vector
     * @param x The translation in x
     * @param y the translation in y
     * @return this
     */
    public Vector2f translate(float x, float y) {
        this.x += x;
        this.y += y;
        return this;
    }

    /**
     * Negate a vector
     * @return this
     */
    public Vector2f negate() {
        x = -x;
        y = -y;
        return this;
    }

    /**
     * Negate a vector and place the result in a destination vector.
     * @param dest The destination vector or null if a new vector is to be created
     * @return the negated vector
     */
    public Vector2f negate(Vector2f dest) {
        if (dest == null)
            dest = new Vector2f();
        dest.x = -x;
        dest.y = -y;
        return dest;
    }


    /**
     * Normalise this vector and place the result in another vector.
     * @param dest The destination vector, or null if a new vector is to be created
     * @return the normalised vector
     */
    public Vector2f normalise(Vector2f dest) {
        float l = length();

        if (dest == null)
            dest = new Vector2f(x / l, y / l);
        else
            dest.set(x / l, y / l);

        return dest;
    }

    /**
     * The dot product of two vectors is calculated as
     * v1.x * v2.x + v1.y * v2.y + v1.z * v2.z
     * @param left The LHS vector
     * @param right The RHS vector
     * @return left dot right
     */
    public static float dot(Vector2f left, Vector2f right) {
        return left.x * right.x + left.y * right.y;
    }



    /**
     * Calculate the angle between two vectors, in radians
     * @param a A vector
     * @param b The other vector
     * @return the angle between the two vectors, in radians
     */
    public static float angle(Vector2f a, Vector2f b) {
        float dls = dot(a, b) / (a.length() * b.length());
        if (dls < -1f)
            dls = -1f;
        else if (dls > 1.0f)
            dls = 1.0f;
        return (float)Math.acos(dls);
    }

    /**
     * Add a vector to another vector and place the result in a destination
     * vector.
     * @param left The LHS vector
     * @param right The RHS vector
     * @param dest The destination vector, or null if a new vector is to be created
     * @return the sum of left and right in dest
     */
    public static Vector2f add(Vector2f left, Vector2f right, Vector2f dest) {
        if (dest == null)
            return new Vector2f(left.x + right.x, left.y + right.y);
        else {
            dest.set(left.x + right.x, left.y + right.y);
            return dest;
        }
    }

    /**
     * Subtract a vector from another vector and place the result in a destination
     * vector.
     * @param left The LHS vector
     * @param right The RHS vector
     * @param dest The destination vector, or null if a new vector is to be created
     * @return left minus right in dest
     */
    public static Vector2f sub(Vector2f left, Vector2f right, Vector2f dest) {
        if (dest == null)
            return new Vector2f(left.x - right.x, left.y - right.y);
        else {
            dest.set(left.x - right.x, left.y - right.y);
            return dest;
        }
    }

    /**
     * Store this vector in a FloatBuffer
     * @param buf The buffer to store it in, at the current position
     * @return this
     */
    public Vector2f store(FloatBuffer buf) {
        buf.put(x);
        buf.put(y);
        return this;
    }

    /**
     * Load this vector from a FloatBuffer
     * @param buf The buffer to load it from, at the current position
     * @return this
     */
    public Vector2f load(FloatBuffer buf) {
        x = buf.get();
        y = buf.get();
        return this;
    }

    /* (non-Javadoc)
     * @see org.lwjgl.vector.Vector#scale(float)
     */
    public Vector2f scale(float scale) {

        x *= scale;
        y *= scale;

        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        StringBuilder sb = new StringBuilder(64);

        sb.append("Vector2f[");
        sb.append(x);
        sb.append(", ");
        sb.append(y);
        sb.append(']');
        return sb.toString();
    }

    /**
     * @return x
     */
    public final float getX() {
        return x;
    }

    /**
     * @return y
     */
    public final float getY() {
        return y;
    }

    /**
     * Set X
     * @param x
     */
    public final void setX(float x) {
        this.x = x;
    }

    /**
     * Set Y
     * @param y
     */
    public final void setY(float y) {
        this.y = y;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Vector2f other = (Vector2f)obj;

        if (x == other.x && y == other.y) return true;

        return false;
    }

}
