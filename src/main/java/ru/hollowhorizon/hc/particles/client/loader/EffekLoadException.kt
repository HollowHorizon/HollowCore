package ru.hollowhorizon.hc.particles.client.loader

class EffekLoadException : RuntimeException {
    constructor(message: String, cause: Throwable? = null) : super(message, cause)
    constructor(cause: Throwable) : super(cause)
}
