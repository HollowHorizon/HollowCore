package ru.hollowhorizon.hc.client.utils.math;

import java.io.Serializable;
import java.nio.FloatBuffer;

public class Vector4f extends Vector<Vector4f> implements Serializable, ReadableVector4f<Vector4f>, WritableVector4f {

    private static final long serialVersionUID = 1L;

    public float x, y, z, w;

    /**
     * Constructor for Vector4f.
     */
    public Vector4f() {
        super();
    }

    /**
     * Constructor
     */
    public Vector4f(ReadableVector4f src) {
        set(src);
    }

    /**
     * Constructor
     */
    public Vector4f(float x, float y, float z, float w) {
        set(x, y, z, w);
    }

    /* (non-Javadoc)
     * @see com.ldtteam.graphicsexpanded.util.math.WritableVector2f#set(float, float)
     */
    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }

    /* (non-Javadoc)
     * @see com.ldtteam.graphicsexpanded.util.math.WritableVector3f#set(float, float, float)
     */
    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /* (non-Javadoc)
     * @see com.ldtteam.graphicsexpanded.util.math.WritableVector4f#set(float, float, float, float)
     */
    public void set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    /**
     * Load from another Vector4f
     * @param src The source vector
     * @return this
     */
    public Vector4f set(ReadableVector4f src) {
        x = src.getX();
        y = src.getY();
        z = src.getZ();
        w = src.getW();
        return this;
    }

    /**
     * @return the length squared of the vector
     */
    public float lengthSquared() {
        return x * x + y * y + z * z + w * w;
    }

    /**
     * Translate a vector
     * @param x The translation in x
     * @param y the translation in y
     * @return this
     */
    public Vector4f translate(float x, float y, float z, float w) {
        this.x += x;
        this.y += y;
        this.z += z;
        this.w += w;
        return this;
    }

    /**
     * Add a vector to another vector and place the result in a destination
     * vector.
     * @param left The LHS vector
     * @param right The RHS vector
     * @param dest The destination vector, or null if a new vector is to be created
     * @return the sum of left and right in dest
     */
    public static Vector4f add(Vector4f left, Vector4f right, Vector4f dest) {
        if (dest == null)
            return new Vector4f(left.x + right.x, left.y + right.y, left.z + right.z, left.w + right.w);
        else {
            dest.set(left.x + right.x, left.y + right.y, left.z + right.z, left.w + right.w);
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
    public static Vector4f sub(Vector4f left, Vector4f right, Vector4f dest) {
        if (dest == null)
            return new Vector4f(left.x - right.x, left.y - right.y, left.z - right.z, left.w - right.w);
        else {
            dest.set(left.x - right.x, left.y - right.y, left.z - right.z, left.w - right.w);
            return dest;
        }
    }


    /**
     * Negate a vector
     * @return this
     */
    public Vector4f negate() {
        x = -x;
        y = -y;
        z = -z;
        w = -w;
        return this;
    }

    /**
     * Negate a vector and place the result in a destination vector.
     * @param dest The destination vector or null if a new vector is to be created
     * @return the negated vector
     */
    public Vector4f negate(Vector4f dest) {
        if (dest == null)
            dest = new Vector4f();
        dest.x = -x;
        dest.y = -y;
        dest.z = -z;
        dest.w = -w;
        return dest;
    }


    /**
     * Normalise this vector and place the result in another vector.
     * @param dest The destination vector, or null if a new vector is to be created
     * @return the normalised vector
     */
    public Vector4f normalise(Vector4f dest) {
        float l = length();

        if (dest == null)
            dest = new Vector4f(x / l, y / l, z / l, w / l);
        else
            dest.set(x / l, y / l, z / l, w / l);

        return dest;
    }

    /**
     * The dot product of two vectors is calculated as
     * v1.x * v2.x + v1.y * v2.y + v1.z * v2.z + v1.w * v2.w
     * @param left The LHS vector
     * @param right The RHS vector
     * @return left dot right
     */
    public static float dot(Vector4f left, Vector4f right) {
        return left.x * right.x + left.y * right.y + left.z * right.z + left.w * right.w;
    }

    /**
     * Calculate the angle between two vectors, in radians
     * @param a A vector
     * @param b The other vector
     * @return the angle between the two vectors, in radians
     */
    public static float angle(Vector4f a, Vector4f b) {
        float dls = dot(a, b) / (a.length() * b.length());
        if (dls < -1f)
            dls = -1f;
        else if (dls > 1.0f)
            dls = 1.0f;
        return (float)Math.acos(dls);
    }

    /* (non-Javadoc)
     * @see org.lwjgl.vector.Vector#load(FloatBuffer)
     */
    public Vector4f load(FloatBuffer buf) {
        x = buf.get();
        y = buf.get();
        z = buf.get();
        w = buf.get();
        return this;
    }

    /* (non-Javadoc)
     * @see org.lwjgl.vector.Vector#scale(float)
     */
    public Vector4f scale(float scale) {
        x *= scale;
        y *= scale;
        z *= scale;
        w *= scale;
        return this;
    }

    /* (non-Javadoc)
     * @see org.lwjgl.vector.Vector#store(FloatBuffer)
     */
    public Vector4f store(FloatBuffer buf) {

        buf.put(x);
        buf.put(y);
        buf.put(z);
        buf.put(w);

        return this;
    }

    public String toString() {
        return "Vector4f: " + x + " " + y + " " + z + " " + w;
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

    /**
     * Set Z
     * @param z
     */
    public void setZ(float z) {
        this.z = z;
    }


    /* (Overrides)
     * @see org.lwjgl.vector.ReadableVector3f#getZ()
     */
    public float getZ() {
        return z;
    }

    /**
     * Set W
     * @param w
     */
    public void setW(float w) {
        this.w = w;
    }

    /* (Overrides)
     * @see org.lwjgl.vector.ReadableVector3f#getZ()
     */
    public float getW() {
        return w;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Vector4f other = (Vector4f)obj;

        if (x == other.x && y == other.y && z == other.z && w == other.w) return true;

        return false;
    }
}
