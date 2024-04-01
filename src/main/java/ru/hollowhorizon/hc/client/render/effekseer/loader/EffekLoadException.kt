package ru.hollowhorizon.hc.client.render.effekseer.loader

class EffekLoadException : RuntimeException {
    constructor(message: String, cause: Throwable? = null) : super(message, cause)
}
