package ru.hollowhorizon.hc.client.gltf

import java.io.InputStream

object GLTFAdapter {
    @JvmStatic
    fun prepare(
        stream: InputStream
    ): InputStream {
        var json = stream.bufferedReader().use { it.readText() }

        //Я понятия не имею, почему при парсинге pbrMetallicRoughness надо переименовывать в extras, ибо при переносе через gson там вообще ни слова про это, но как вариант есть такой костыль
        json = json.replace("pbrMetallicRoughness", "extras")

        return json.byteInputStream()
    }
}