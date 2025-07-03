package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.Assets
import de.fabmax.kool.KoolConfigJvm
import de.fabmax.kool.KoolSystem
import de.fabmax.kool.loadImage2d
import de.fabmax.kool.pipeline.*
import de.fabmax.kool.util.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import net.minecraft.resources.ResourceLocation
import org.apache.logging.log4j.LogManager

import org.apache.logging.log4j.Logger
import ru.hollowhorizon.hc.client.kool.minecraft.ImageManager
import ru.hollowhorizon.hc.client.kool.minecraft.MCAssetLoader
import ru.hollowhorizon.hc.common.utils.json.JsonFormat
import ru.hollowhorizon.hc.common.utils.rl
import ru.hollowhorizon.hc.client.utils.stream
import ru.hollowhorizon.hc.common.events.Event
import ru.hollowhorizon.hc.common.events.post

@OptIn(ExperimentalSerializationApi::class)
object KoolManager {
    val LOGGER: Logger = LogManager.getLogger()

    init {
        Log.printer = LogPrinter { level, tag, message ->
            LOGGER.info("[$level] $tag: $message")
        }
        KoolSystem.initialize(KoolConfigJvm(defaultAssetLoader = MCAssetLoader))

        KoolInitEvent().post()
    }

    val context = MCKoolContext()
    val MONOCRAFT by lazy {
        val fontInfo = JsonFormat.decodeFromStream<MsdfMeta>("hollowcore:fonts/monocraft.json".rl.stream)
        val msdfMap = Texture2d(TexFormat.RGBA, MipMapping.Off, SamplerSettings(), "MsdfFont:${fontInfo.name}") {
            Assets.loadImage2d("fonts/monocraft.png")
                .getOrDefault(SingleColorTexture.getColorTextureData(Color.BLACK))
        }
        MsdfFontData(msdfMap, fontInfo)
    }
}

class KoolInitEvent: Event {
    fun loadTexture(texture: ResourceLocation) {
        ImageManager.load(texture.toString())
    }
}