package ru.hollowhorizon.hc.client.utils.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject

class JsonBuilder(val obj: JsonObject) {
    infix fun String.to(json: JsonElement) = obj.add(this, json)
    infix fun String.to(value: Number) = obj.addProperty(this, value)
    infix fun String.to(value: Char) = obj.addProperty(this, value)
    infix fun String.to(value: String) = obj.addProperty(this, value)
    infix fun String.to(value: Boolean) = obj.addProperty(this, value)
    infix operator fun String.invoke(json: JsonElement) = obj.add(this, json)
    infix operator fun String.invoke(value: JsonBuilder.() -> Unit) = obj.add(this, JsonObject()
        .apply { JsonBuilder(this).value() }
    )

    infix operator fun String.invoke(value: Number) = obj.addProperty(this, value)
    infix operator fun String.invoke(value: Char) = obj.addProperty(this, value)
    infix operator fun String.invoke(value: String) = obj.addProperty(this, value)
    infix operator fun String.invoke(value: Boolean) = obj.addProperty(this, value)

    fun array(vararg elements: JsonElement) = JsonArray().apply { elements.forEach(::add) }
    fun array(vararg elements: Number) = JsonArray().apply { elements.forEach(::add) }
    fun array(vararg elements: String) = JsonArray().apply { elements.forEach(::add) }
    fun array(vararg elements: Char) = JsonArray().apply { elements.forEach(::add) }
    fun array(vararg elements: Boolean) = JsonArray().apply { elements.forEach(::add) }
    fun array(vararg elements: JsonBuilder.() -> Unit) = JsonArray().apply {
        elements.forEach { add(JsonObject().apply { JsonBuilder(this).it() }) }
    }
}

fun json(builder: JsonBuilder.() -> Unit): JsonObject {
    val jsonBuilder = JsonBuilder(JsonObject())
    jsonBuilder.builder()
    return jsonBuilder.obj
}

fun json(vararg elements: JsonElement) = JsonArray().apply { elements.forEach(::add) }
fun json(vararg elements: Number) = JsonArray().apply { elements.forEach(::add) }
fun json(vararg elements: String) = JsonArray().apply { elements.forEach(::add) }
fun json(vararg elements: Char) = JsonArray().apply { elements.forEach(::add) }
fun json(vararg elements: Boolean) = JsonArray().apply { elements.forEach(::add) }