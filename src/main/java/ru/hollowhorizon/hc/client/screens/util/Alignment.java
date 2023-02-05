package ru.hollowhorizon.hc.client.screens.util;

public enum Alignment implements IPlacement {
    BOTTOM_CENTER(0.5F, 1.0F),
    BOTTOM_RIGHT(1F, 1.0F),
    BOTTOM_LEFT(0F, 1.0F),

    CENTER(0.5F, 0.5F),
    RIGHT_CENTER(1F, 0.5F),
    LEFT_CENTER(0F, 0.5F),

    TOP_CENTER(0.5F, 0F),
    TOP_RIGHT(1F, 0F),
    TOP_LEFT(0F, 0F);


    private final float factorX;
    private final float factorY;

    Alignment(float factorX, float factorY) {
        this.factorX = factorX;
        this.factorY = factorY;
    }

    public static IPlacement custom(float factorX, float factorY) {
        return new IPlacement() {

            @Override
            public float factorX() {
                return factorX;
            }

            @Override
            public float factorY() {
                return factorY;
            }
        };
    }

    @Override
    public float factorX() {
        return factorX;
    }

    @Override
    public float factorY() {
        return factorY;
    }
}
