package ru.hollowhorizon.hc.client.kool

import com.mojang.blaze3d.platform.GlStateManager
import de.fabmax.kool.Assets
import de.fabmax.kool.KoolApplication
import de.fabmax.kool.KoolConfigJvm
import de.fabmax.kool.KoolSystem
import de.fabmax.kool.pipeline.AsyncTextureLoader
import de.fabmax.kool.pipeline.CullMethod
import de.fabmax.kool.pipeline.DepthCompareOp
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.pipeline.backend.gl.glOp
import de.fabmax.kool.util.Log
import de.fabmax.kool.util.MsdfFont.Companion.MSDF_TEX_PROPS
import de.fabmax.kool.util.MsdfFontData
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import org.apache.logging.log4j.LogManager
import org.lwjgl.opengl.GL33
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
        KoolSystem.initialize(
            KoolConfigJvm(
                defaultAssetLoader = MCAssetLoader
            )
        )
        isKoolLoaded = true
    }

    val ctx = MCKoolContext()
    val MONOCRAFT_DATA by lazy {
        val fontInfo = KoolSystem.config.defaultFont
        val msdfMap = Texture2d(
            props = MSDF_TEX_PROPS,
            name = "MsdfFont:${fontInfo.fontMeta.name}",
            loader = AsyncTextureLoader { Assets.loadTextureData("fonts/monocraft.png", MSDF_TEX_PROPS) }
        )
        MsdfFontData(msdfMap, JsonFormat.decodeFromStream("hollowcore:fonts/monocraft.json".rl.stream))
    }

    var actIsWriteDepth = true
    var actDepthTest: DepthCompareOp = DepthCompareOp.LESS_EQUAL
    var actCullMethod: CullMethod = CullMethod.NO_CULLING
    var lineWidth = 1f

    fun update() {
        return
        MCGlApi.clipControl(MCGlApi.LOWER_LEFT, MCGlApi.ZERO_TO_ONE)
        val activeTexture = GlStateManager._getActiveTexture()
        val currentTexture = GL33.glGetInteger(GL33.GL_TEXTURE_BINDING_2D)
        val currentVAO = GL33.glGetInteger(GL33.GL_VERTEX_ARRAY_BINDING)
        val currentElementArrayBuffer = GL33.glGetInteger(GL33.GL_ELEMENT_ARRAY_BUFFER_BINDING)

        MCGlApi.depthMask(actIsWriteDepth)
        if (actDepthTest == DepthCompareOp.ALWAYS) {
            MCGlApi.disable(MCGlApi.DEPTH_TEST)
        } else {
            MCGlApi.enable(MCGlApi.DEPTH_TEST)
            MCGlApi.depthFunc(actDepthTest.glOp(MCGlApi))
        }
        when (actCullMethod) {
            CullMethod.CULL_BACK_FACES -> {
                MCGlApi.enable(MCGlApi.CULL_FACE)
                MCGlApi.cullFace(MCGlApi.BACK)
            }

            CullMethod.CULL_FRONT_FACES -> {
                MCGlApi.enable(MCGlApi.CULL_FACE)
                MCGlApi.cullFace(MCGlApi.FRONT)
            }

            CullMethod.NO_CULLING -> MCGlApi.disable(MCGlApi.CULL_FACE)
        }
        MCGlApi.lineWidth(lineWidth)

        ctx.renderFrame()

        // Необходимо сохранять эти параметры, поскольку и майн и движок кешируют их
        actIsWriteDepth = GL33.glGetBoolean(GL33.GL_DEPTH_WRITEMASK)
        actDepthTest = getCurrentDepthCompareOp()
        actCullMethod = getCurrentCullMethod()
        lineWidth = GL33.glGetFloat(GL33.GL_LINE_WIDTH)

        GL33.glActiveTexture(activeTexture)
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, currentTexture)
        GL33.glBindVertexArray(currentVAO)
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, currentElementArrayBuffer)
        MCGlApi.clipControl(MCGlApi.LOWER_LEFT, MCGlApi.NEGATIVE_ONE_TO_ONE)

        GL33.glEnable(GL33.GL_DEPTH_TEST)
        GL33.glDepthFunc(GL33.GL_LEQUAL)
        GL33.glEnable(GL33.GL_BLEND)
        GL33.glBlendFuncSeparate(
            GL33.GL_SRC_ALPHA,
            GL33.GL_ONE_MINUS_SRC_ALPHA,
            GL33.GL_ONE,
            GL33.GL_ONE_MINUS_SRC_ALPHA
        )
        GL33.glEnable(GL33.GL_CULL_FACE)
    }

    private fun getCurrentDepthCompareOp() = when (GL33.glGetInteger(GL33.GL_DEPTH_FUNC)) {
        GL33.GL_ALWAYS -> DepthCompareOp.ALWAYS
        GL33.GL_NEVER -> DepthCompareOp.NEVER
        GL33.GL_LESS -> DepthCompareOp.LESS
        GL33.GL_LEQUAL -> DepthCompareOp.LESS_EQUAL
        GL33.GL_GREATER -> DepthCompareOp.GREATER
        GL33.GL_GEQUAL -> DepthCompareOp.GREATER_EQUAL
        GL33.GL_EQUAL -> DepthCompareOp.EQUAL
        GL33.GL_NOTEQUAL -> DepthCompareOp.NOT_EQUAL
        else -> throw IllegalStateException("Unknown depth compare operation")
    }

    fun getCurrentCullMethod(): CullMethod {
        if (!GL33.glIsEnabled(GL33.GL_CULL_FACE)) return CullMethod.NO_CULLING
        return when (GL33.glGetInteger(GL33.GL_CULL_FACE_MODE)) {
            GL33.GL_BACK -> CullMethod.CULL_BACK_FACES
            GL33.GL_FRONT -> CullMethod.CULL_FRONT_FACES
            else -> CullMethod.NO_CULLING // На случай необычного значения.
        }
    }

    suspend fun app(appBlock: suspend KoolApplication.() -> Unit) {
        val app = KoolApplication(ctx)
        appBlock.invoke(app)
    }
}