package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.Assets
import de.fabmax.kool.KoolConfigJvm
import de.fabmax.kool.KoolSystem
import de.fabmax.kool.pipeline.AsyncTextureLoader
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.util.Log
import de.fabmax.kool.util.MsdfFont.Companion.MSDF_TEX_PROPS
import de.fabmax.kool.util.MsdfFontData
import de.fabmax.kool.util.MsdfMeta
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.apache.logging.log4j.LogManager
import ru.hollowhorizon.hc.client.utils.json.JsonFormat
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.stream

var isKoolLoaded = false

@OptIn(ExperimentalSerializationApi::class)
object KoolManager {
    val LOGGER = LogManager.getLogger()

    init {
        Log.printer = { level, tag, message ->
            LOGGER.info("[$level] $tag: $message")
        }
        KoolSystem.initialize(KoolConfigJvm(defaultAssetLoader = MCAssetLoader))
        isKoolLoaded = true
    }

    val ctx = MCKoolContext()
    val MONOCRAFT_DATA by lazy {
        val fontInfo = JsonFormat.decodeFromStream<MsdfMeta>("hollowcore:fonts/monocraft.json".rl.stream)
        val msdfMap = Texture2d(
            props = MSDF_TEX_PROPS,
            name = "MsdfFont:${fontInfo.name}",
            loader = AsyncTextureLoader { Assets.loadTextureData("fonts/monocraft.png", MSDF_TEX_PROPS) }
        )
        MsdfFontData(msdfMap, fontInfo)
    }
}