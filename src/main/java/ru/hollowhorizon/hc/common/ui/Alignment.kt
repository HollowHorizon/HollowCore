package ru.hollowhorizon.hc.common.ui

enum class Alignment(override val factorX: Float, override val factorY: Float): IPlacement {
    BOTTOM_CENTER(0.5F, 1.0F),
    BOTTOM_RIGHT(1F, 1.0F),
    BOTTOM_LEFT(0F, 1.0F),

    CENTER(0.5F, 0.5F),
    RIGHT_CENTER(1F, 0.5F),
    LEFT_CENTER(0F, 0.5F),

    TOP_CENTER(0.5F, 0F),
    TOP_RIGHT(1F, 0F),
    TOP_LEFT(0F, 0F);
}


enum class Anchor(value: Float) {
    START(0f),
    CENTER(0.5f),
    END(1f)
}


interface IPlacement {
    val factorX: Float
    val factorY: Float
}

