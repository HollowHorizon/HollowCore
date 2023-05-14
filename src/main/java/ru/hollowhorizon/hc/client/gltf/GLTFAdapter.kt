package ru.hollowhorizon.hc.client.gltf

import com.google.gson.JsonParser
import net.minecraft.util.math.vector.Vector3f
import ru.hollowhorizon.hc.common.capabilities.AnimatedEntityCapability
import java.io.InputStream

object GLTFAdapter {
    @JvmStatic
    fun prepare(
        stream: InputStream,
        capability: AnimatedEntityCapability
    ): InputStream {
        var json = stream.bufferedReader().use { it.readText() }

        val generator = JsonParser().parse(json).asJsonObject.getAsJsonObject("asset").get("generator").asString

        //Немного костыльная проверка на то, экспортирована ли модель из BlockBench
        if("Blockbench" in generator && "glTF exporter" in generator) {
            capability.transform.multiply(Vector3f.YP.rotationDegrees(180f)) //Поворот модели на 180 градусов, ибо какого-то хера BlockBench экспортирует модель в обратном направлении и все мобы ходят задом...
        }
        //Я понятия не имею, почему при парсинге pbrMetallicRoughness надо переименовывать в extras, ибо при переносе через gson там вообще ни слова про это, но как вариант есть такой костыль
        json = json.replace("pbrMetallicRoughness", "extras")

        return json.byteInputStream()
    }
}