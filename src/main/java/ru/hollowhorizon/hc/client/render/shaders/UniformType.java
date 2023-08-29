package ru.hollowhorizon.hc.client.render.shaders;

import java.util.function.BooleanSupplier;

public enum UniformType {
    INT(Carrier.INT, () -> true, 1),
    U_INT(Carrier.U_INT, () -> true, 1),
    FLOAT(Carrier.FLOAT, () -> true, 1),

    VEC2(Carrier.FLOAT, () -> true, 2),
    I_VEC2(Carrier.INT, () -> true, 2),
    U_VEC2(Carrier.U_INT, () -> true, 2),
    B_VEC2(Carrier.INT, () -> true, 2),

    VEC3(Carrier.FLOAT, () -> true, 3),
    I_VEC3(Carrier.INT, () -> true, 3),
    U_VEC3(Carrier.U_INT, () -> true, 3),
    B_VEC3(Carrier.INT, () -> true, 3),

    VEC4(Carrier.FLOAT, () -> true, 4),
    I_VEC4(Carrier.INT, () -> true, 4),
    U_VEC4(Carrier.U_INT, () -> true, 4),
    B_VEC4(Carrier.INT, () -> true, 4),

    MAT2(Carrier.MATRIX, () -> true, 2 * 2),
    MAT2x3(Carrier.MATRIX, () -> true, 2 * 3),
    MAT2x4(Carrier.MATRIX, () -> true, 2 * 4),

    MAT3(Carrier.MATRIX, () -> true, 3 * 3),
    MAT3x2(Carrier.MATRIX, () -> true, 3 * 2),
    MAT3x4(Carrier.MATRIX, () -> true, 3 * 4),

    MAT4(Carrier.MATRIX, () -> true, 4 * 4),
    MAT4x2(Carrier.MATRIX, () -> true, 4 * 2),
    MAT4x3(Carrier.MATRIX, () -> true, 4 * 3),

    DOUBLE(Carrier.DOUBLE, () -> true, 1),
    D_VEC2(Carrier.DOUBLE, () -> true, 2),
    D_VEC3(Carrier.DOUBLE, () -> true, 3),
    D_VEC4(Carrier.DOUBLE, () -> true, 4),

    D_MAT2(Carrier.D_MATRIX, () -> true, 2 * 2),
    D_MAT2x3(Carrier.D_MATRIX, () -> true, 2 * 3),
    D_MAT2x4(Carrier.D_MATRIX, () -> true, 2 * 4),

    D_MAT3(Carrier.D_MATRIX, () -> true, 3 * 3),
    D_MAT3x2(Carrier.D_MATRIX, () -> true, 3 * 2),
    D_MAT3x4(Carrier.D_MATRIX, () -> true, 3 * 4),

    D_MAT4(Carrier.D_MATRIX, () -> true, 4 * 4),
    D_MAT4x2(Carrier.D_MATRIX, () -> true, 4 * 2),
    D_MAT4x3(Carrier.D_MATRIX, () -> true, 4 * 3);

    private final Carrier carrier;
    private final int size;
    private BooleanSupplier func;
    private boolean isSupported;

    UniformType(Carrier carrier, BooleanSupplier func, int size) {
        this.carrier = carrier;
        this.func = func;
        this.size = size;
    }

    public Carrier getCarrier() {
        return carrier;
    }

    public int getSize() {
        return size;
    }

    public boolean isSupported() {
        if (func != null) {
            isSupported = func.getAsBoolean();
            func = null;
        }
        return isSupported;
    }

    public enum Carrier {
        INT,
        U_INT,
        FLOAT,
        DOUBLE,
        MATRIX,
        D_MATRIX
    }
}
