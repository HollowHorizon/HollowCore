package ru.hollowhorizon.hc

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import ru.hollowhorizon.hc.common.events.EventBus
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.RegisterCommandsEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterEntityAttributesEvent
import java.io.File

object FabricEvents {
    init {
        registerAttributes()
        registerCommands()
    }

    private fun registerAttributes() {
        val attributes = RegisterEntityAttributesEvent()
        EventBus.post(attributes)
        attributes.getAttributes().forEach(FabricDefaultAttributeRegistry::register)
    }

    private fun registerCommands() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, registryAccess, environment ->
            RegisterCommandsEvent(dispatcher, registryAccess, environment).post()
        })
    }
}

fun main() {
    @Serializable
    class Color(val r: Float, val g: Float, val b: Float, val a: Float)

    @Serializable
    class ThemeConfig(
        var mainColor: Color,
        var hoveredColor: Color,
    )

    val text = Json { prettyPrint = true }.encodeToString(
        ThemeConfig(
            Color(0.4f, 0.4f, 0.4f, 0.4f),
            Color(0.14f, 0.41f, 0.5324f, 0.94f)
        )
    )

    val file = File("default.json")

    file.writeText(text)
    val config = Json.decodeFromStream<ThemeConfig>(file.inputStream())
}