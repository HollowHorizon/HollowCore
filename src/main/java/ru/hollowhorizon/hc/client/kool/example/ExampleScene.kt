package ru.hollowhorizon.hc.client.kool.example

import de.fabmax.kool.Assets
import de.fabmax.kool.loadImage2d
import de.fabmax.kool.modules.ui2.*
import de.fabmax.kool.modules.ui2.docking.UiDockable
import de.fabmax.kool.pipeline.SamplerSettings
import de.fabmax.kool.pipeline.Texture2d
import de.fabmax.kool.pipeline.TextureProps
import de.fabmax.kool.scene.Scene
import de.fabmax.kool.toString
import de.fabmax.kool.util.DebugOverlay
import de.fabmax.kool.util.MsdfFont
import ru.hollowhorizon.hc.client.kool.KoolDrawer
import ru.hollowhorizon.hc.client.kool.KoolManager
import ru.hollowhorizon.hc.client.kool.KoolManager.MONOCRAFT_DATA
import ru.hollowhorizon.hc.client.kool.mcCamera
import kotlin.math.min
import kotlin.math.roundToInt

private val beeTex by lazy {
    Texture2d(
        TextureProps(
            generateMipMaps = false,
            defaultSamplerSettings = SamplerSettings().clamped().nearest()
        )
    ) {
        Assets.loadImage2d("hollowcore:textures/bee.png").getOrThrow()
    }
}

@JvmField
var isBeesEnabled = false

@JvmField
val BEE_DRAWER = KoolDrawer {
    clearColor = null
    clearDepth = false

    mcCamera()

    val bees = GpuBees(this)
    bees.setupShaders(beeTex)

    addNode(bees.beeMeshA)
    addNode(bees.beeMeshB)
    bees.setEnabled(true)

    val maxBees = BeeConfig.maxBeesPerTeamGpu
    BeeConfig.beesPerTeam.set(min(BeeConfig.beesPerTeam.value, maxBees))

    val dbgOverlay = DebugOverlay(DebugOverlay.Position.LOWER_RIGHT)
    KoolManager.ctx.addScene(dbgOverlay.ui)
    KoolManager.ctx.addScene(Scene("UI").apply {
        setupUiScene()

        val dockable = UiDockable("Settings")
        addWindowSurface(dockable, sizes = Sizes.medium.copy(normalText = MsdfFont(MONOCRAFT_DATA, 30f))) {
            Column {
                TitleBar(dockable)
                MenuSlider2(
                    "Number of bees: ",
                    BeeConfig.beesPerTeam.use().toFloat(),
                    10f,
                    BeeConfig.maxBeesPerTeamGpu.toFloat(),
                    { "${it.roundToInt() * 2}" }
                ) {
                    BeeConfig.beesPerTeam.set(it.roundToInt())
                }
            }
        }
    })
}

fun UiScope.MenuSlider2(
    label: String,
    value: Float,
    min: Float,
    max: Float,
    txtFormat: (Float) -> String = { it.toString(2) },
    onChange: (Float) -> Unit,
) {
    Row {
        modifier.margin(10.dp)

        Text(label) { modifier.font(MsdfFont(MONOCRAFT_DATA, 30f)) }
        Text(txtFormat(value)) { modifier.font(MsdfFont(MONOCRAFT_DATA, 30f)) }
    }
    Row(Grow.Std) {
        modifier.margin(10.dp)

        Slider(value, min, max) {
            modifier
                .width(Grow.Std)
                .alignY(AlignmentY.Center)
                .onChange(onChange)
        }
    }
}