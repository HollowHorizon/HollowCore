package ru.hollowhorizon.hc.common.config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.properties.Delegates
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Config<T : Saveable>(
    creator: () -> T,
    name: String,
    val decoder: (InputStream) -> T,
    val encoder: (T) -> String,
) : ReadOnlyProperty<Any?, T> {
    val file = File("").resolve("config/$name.toml")
    var value: T

    init {
        if (file.exists()) {
            FileInputStream(file).use {
                value = decoder(it)
            }
        } else {
            value = creator()
        }

        value.save = {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            file.writeText(encoder(value))
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }
}

inline fun <reified T : Saveable> hollowConfig(noinline value: () -> T, name: String): ReadOnlyProperty<Any?, T> {
    val decoder: (InputStream) -> T = { inputStream: InputStream ->
        val text = inputStream.bufferedReader().readText()
        TomlFormat.decodeFromString<T>(text)
    }
    val encoder: (T) -> String = { variable: T ->
        TomlFormat.encodeToString(variable)
    }
    return Config(value, name, decoder, encoder)
}

abstract class HollowConfig : Saveable {
    override lateinit var save: () -> Unit

    fun <T> property(value: T): ReadWriteProperty<HollowCoreConfig, T> {
        return Delegates.observable(value) { prop, old, new ->
            if (old != new) {
                save()
            }
        }
    }
}

interface Saveable {
    var save: () -> Unit
}