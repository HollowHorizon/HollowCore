package ru.hollowhorizon.hc.client.kool

import de.fabmax.kool.Assets
import de.fabmax.kool.KoolConfigJvm
import de.fabmax.kool.KoolSystem
import de.fabmax.kool.loadImage2d
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.util.Log
import de.fabmax.kool.util.MsdfFont.Companion.MSDF_TEX_PROPS
import de.fabmax.kool.util.MsdfFontData
import de.fabmax.kool.util.MsdfMeta
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import ru.hollowhorizon.hc.client.kool.minecraft.MCAssetLoader
import ru.hollowhorizon.hc.client.utils.json.JsonFormat
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.client.utils.stream

@OptIn(ExperimentalSerializationApi::class)
object KoolManager {
    val LOGGER: Logger = LogManager.getLogger()

    init {
        Log.printer = { level, tag, message ->
            LOGGER.info("[$level] $tag: $message")
        }
        KoolSystem.initialize(KoolConfigJvm(defaultAssetLoader = MCAssetLoader))
    }

    val context = MCKoolContext()
    val MONOCRAFT_DATA by lazy {
        val fontInfo = JsonFormat.decodeFromStream<MsdfMeta>("hollowcore:fonts/monocraft.json".rl.stream)
        val msdfMap = Texture2d(MSDF_TEX_PROPS, "MsdfFont:${fontInfo.name}") {
            Assets.loadImage2d("fonts/monocraft.png", MSDF_TEX_PROPS).getOrThrow()
        }
        MsdfFontData(msdfMap, fontInfo)
    }
}