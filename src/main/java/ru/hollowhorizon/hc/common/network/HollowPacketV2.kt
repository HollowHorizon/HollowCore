package ru.hollowhorizon.hc.common.network


@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
annotation class HollowPacketV2(val toTarget: Direction = Direction.ANY) {
    enum class Direction { TO_CLIENT, TO_SERVER, ANY }
}