package ru.hollowhorizon.hc.client.utils.math;

public interface WritableVector4f extends WritableVector3f {

    void setW(float w);

    void set(float x, float y, float z, float w);

}
