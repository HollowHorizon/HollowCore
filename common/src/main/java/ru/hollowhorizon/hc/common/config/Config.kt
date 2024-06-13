package ru.hollowhorizon.hc.common.config

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import ru.hollowhorizon.hc.HollowCore
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import kotlin.properties.ReadOnlyProperty
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
        var save = false
        if (file.exists()) {
            HollowCore.LOGGER.info("Loading config: $name.toml")
            try {
                FileInputStream(file).use {
                    value = decoder(it)
                }
            } catch (e: Exception) {
                HollowCore.LOGGER.warn("Error when loading config '{}':", "$name.toml", e)
                value = creator()
                save = true
            }
        } else {
            value = creator()
            save = true
        }

        value.save = {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            file.writeText(encoder(value))
        }
        if (save) value.save()
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
}

interface Saveable {
    var save: () -> Unit
}