package ru.hollowhorizon.hc.client.utils

import ru.hollowhorizon.hc.HollowCore

class HollowLogger(val name: String) {
    fun info() {
        HollowCore.LOGGER.info(name)
    }

    fun info(message: String) {
        HollowCore.LOGGER.info("$name: $message")
    }

    fun warn() {
        HollowCore.LOGGER.warn(name)
    }
}

fun String.log(): HollowLogger {
    return HollowLogger(this)
}