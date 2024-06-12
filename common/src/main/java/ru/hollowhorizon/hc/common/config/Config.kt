package ru.hollowhorizon.hc.common.config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Config<T>(var value: T, name: String, val decoder: (InputStream) -> T, val encoder: (T) -> String) :
    ReadWriteProperty<Any?, T> {
    val file = File("").resolve("config/$name.toml")

    init {
        if (file.exists()) {
            FileInputStream(file).use {
                value = decoder(it)
            }
        }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value

        if (!file.exists()) {
            file.parentFile.mkdirs()
            file.createNewFile()
        }
        file.writeText(encoder(value))
    }
}

inline fun <reified T> autoConfig(value: T, name: String): ReadWriteProperty<Any?, T> {
    val decoder: (InputStream) -> T = { inputStream: InputStream ->
        val text = inputStream.bufferedReader().readText()
        TomlFormat.decodeFromString<T>(text)
    }
    val encoder: (T) -> String = { variable: T ->
        TomlFormat.encodeToString(variable)
    }
    return Config(value, name, decoder, encoder)
}