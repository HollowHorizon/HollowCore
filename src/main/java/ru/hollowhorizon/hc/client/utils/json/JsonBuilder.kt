/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.utils.json

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.serialization.json.Json
import net.minecraft.resources.ResourceLocation
import ru.hollowhorizon.hc.client.utils.stream
import java.io.InputStream
import java.io.InputStreamReader

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

fun json(stream: InputStream): JsonElement = JsonParser.parseReader(InputStreamReader(stream))

fun json(location: ResourceLocation) = json(location.stream)

val JsonFormat = Json {
    prettyPrint = true
}