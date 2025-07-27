package ru.hollowhorizon.hc.common.utils.molang.runtime

class Variables {
    private val values = mutableMapOf<String, Float>()

    init {
        values["math.pi"] = Math.pi
        values["pi"] = Math.pi
    }

    operator fun get(name: String): Float = values[name.lowercase()] ?: error("Variable '$name' not found")

    operator fun set(name: String, value: Float) {
        values[name] = value
    }

    operator fun contains(name: String): Boolean =
        values.containsKey(name)

    fun names(): Set<String> =
        values.keys

    override fun toString(): String =
        values.entries.joinToString(
            prefix = "Variables [", postfix = "]", separator = ", "
        ) { "${it.key}=${it.value}" }
}