package ru.hollowhorizon.hc.client.gltf.animations

enum class PresetType {
    IDLE, IDLE_SNEAKED, WALK, WALK_SNEAKED,
    RUN, SWIM, FALL, FLY, SIT, SLEEP, SWING, DEATH
}

enum class AnimationType {
    ONCE, //Одиночный запуск анимации
    LOOPED, //После завершения анимация начнётся с начала
    LAST_FRAME, //После завершения анимация застынет на последнем кадре
    PING_PONG; //После завершения анимация начнёт проигрываться в обратном порядке


    fun stopOnEnd(): Boolean = this == ONCE
}