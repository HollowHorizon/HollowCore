package ru.hollowhorizon.hc.common.ui

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import net.minecraft.client.Minecraft
import net.minecraft.world.entity.player.Player
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.network.PacketDistributor
import net.minecraftforge.server.ServerLifecycleHooks
import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.api.utils.Polymorphic
import ru.hollowhorizon.hc.client.screens.UIScreen
import ru.hollowhorizon.hc.client.utils.ScissorUtil
import ru.hollowhorizon.hc.client.utils.nbt.NBTFormat
import ru.hollowhorizon.hc.client.utils.nbt.serialize
import ru.hollowhorizon.hc.client.utils.use
import ru.hollowhorizon.hc.common.network.HollowPacketV2
import ru.hollowhorizon.hc.common.network.HollowPacketV3
import ru.hollowhorizon.hc.common.ui.animations.UIAnimation
import ru.hollowhorizon.hc.common.ui.widgets.button
import ru.hollowhorizon.hc.common.ui.widgets.entity
import ru.hollowhorizon.hc.common.ui.widgets.image

fun gui(builder: Widget.() -> Unit) = Widget().apply(builder)
fun Widget.gui(builder: Widget.() -> Unit) {
    this += Widget().apply(builder)
}

interface IWidget {

    @OnlyIn(Dist.CLIENT)
    fun render(
        stack: PoseStack,
        x: Int, y: Int,
        screenWidth: Int, screenHeight: Int,
        widgetWidth: Int, widgetHeight: Int,
        mouseX: Int, mouseY: Int, partialTick: Float,
    )

    @OnlyIn(Dist.CLIENT)
    fun renderWidget(
        stack: PoseStack,
        x: Int, y: Int,
        screenWidth: Int, screenHeight: Int,
        widgetWidth: Int, widgetHeight: Int,
        mouseX: Int, mouseY: Int, partialTick: Float,
    ) {
    }

    @OnlyIn(Dist.CLIENT)
    fun buttonPressed(
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
        widgetWidth: Int,
        widgetHeight: Int,
        mouseX: Int,
        mouseY: Int,
    ): Boolean = false

    fun widgetButtonPressed(
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
        widgetWidth: Int,
        widgetHeight: Int,
        mouseX: Int,
        mouseY: Int,
    ): Boolean = false
}

@Serializable
@Polymorphic(IWidget::class)
open class Widget : IWidget {
    internal val widgets = ArrayList<IWidget>()
    internal val animations = ArrayList<UIAnimation>()
    var offsetX: ScreenPosition = 0.px
    var offsetY: ScreenPosition = 0.px
    var width: ScreenPosition = 200.px
    var height: ScreenPosition = 100.px
    var padding = Padding(0.px, 0.px, 0.px, 0.px)
    var alignment = Alignment.CENTER
    var enableScissors = false
    var zLayer = 0
    var enableDepth = false
    var visible = true

    @OnlyIn(Dist.CLIENT)
    override fun render(
        stack: PoseStack,
        x: Int, y: Int,
        screenWidth: Int, screenHeight: Int,
        widgetWidth: Int, widgetHeight: Int,
        mouseX: Int, mouseY: Int, partialTick: Float,
    ) = stack.use {
        if(enableDepth) RenderSystem.enableDepthTest()
        translate(0.0, 0.0, zLayer.toDouble())

        if (!visible) return@use

        val width = width(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val height = height(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val offsetX = offsetX(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val offsetY = offsetY(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val x = if(this@Widget.offsetX is ScreenPosition.Mouse) offsetX else x + offsetX
        val y = if(this@Widget.offsetY is ScreenPosition.Mouse) offsetY else y + offsetY
        val nx = (x + widgetWidth * alignment.factorX - width * alignment.factorX).toInt()
        val ny = (y + widgetHeight * alignment.factorY - height * alignment.factorY).toInt()

        if (enableScissors) ScissorUtil.push(nx, ny, width, height)

        renderWidget(stack, nx, ny, screenWidth, screenHeight, width, height, mouseX, mouseY, partialTick)

        val left = padding.left(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val right = padding.right(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val top = padding.top(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val bottom = padding.bottom(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)

        widgets.forEach {
            it.render(
                stack,
                nx + left,
                ny + top,
                screenWidth,
                screenHeight,
                width - right,
                height - bottom,
                mouseX,
                mouseY,
                partialTick
            )
        }
        if (enableScissors) ScissorUtil.pop()
        if(enableDepth) RenderSystem.disableDepthTest()
    }

    override fun buttonPressed(
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
        widgetWidth: Int,
        widgetHeight: Int,
        mouseX: Int,
        mouseY: Int,
    ): Boolean {
        if (!visible) return false

        val width = width(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val height = height(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val offsetX = offsetX(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val offsetY = offsetY(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val nx = (x + offsetX + widgetWidth * alignment.factorX - width * alignment.factorX).toInt()
        val ny = (y + offsetY + widgetHeight * alignment.factorY - height * alignment.factorY).toInt()

        val left = padding.left(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val right = padding.right(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val top = padding.top(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)
        val bottom = padding.bottom(screenWidth, screenHeight, widgetWidth, widgetHeight, mouseX, mouseY)

        val isPressed = widgets.map { it as Widget }.sortedByDescending { it.zLayer }.any {
            it.buttonPressed(
                nx + left,
                ny + top,
                screenWidth,
                screenHeight,
                width - right,
                height - bottom,
                mouseX,
                mouseY
            )
        }

        return isPressed || widgetButtonPressed(
            nx,
            ny,
            screenWidth,
            screenHeight,
            width,
            height,
            mouseX,
            mouseY
        )
    }

    fun animations(vararg animations: UIAnimation) {
        this.animations.addAll(animations)
    }

    fun size(x: ScreenPosition, y: ScreenPosition) {
        x.isWidth = true
        y.isWidth = false
        width = x
        height = y
    }

    fun offset(x: ScreenPosition, y: ScreenPosition) {
        x.isWidth = true
        y.isWidth = false
        offsetX = x
        offsetY = y
    }

    fun padding(
        top: ScreenPosition = 0.px,
        bottom: ScreenPosition = 0.px,
        left: ScreenPosition = 0.px,
        right: ScreenPosition = 0.px,
    ) {
        top.isWidth = false
        bottom.isWidth = false
        left.isWidth = true
        right.isWidth = true
        padding = Padding(top, bottom, left, right)
    }

    fun align(align: Alignment) {
        alignment = align
    }

    val Int.px get() = ScreenPosition.Pixels(this)
    val Int.pw get() = ScreenPosition.PercentWidget(this / 100f, false)
    val Int.ps get() = ScreenPosition.PercentScreen(this / 100f, false)
    val mouse get() = ScreenPosition.Mouse(false)
    operator fun ScreenPosition.plus(other: ScreenPosition) = ScreenPosition.Addition(this, other)
    operator fun ScreenPosition.minus(other: ScreenPosition) = ScreenPosition.Subtraction(this, other)

    operator fun plusAssign(widget: Widget) {
        widgets += widget
    }

    @Serializable
    class Padding(
        val top: ScreenPosition,
        val bottom: ScreenPosition,
        val left: ScreenPosition,
        val right: ScreenPosition,
    )

    fun getPathToNode(target: Widget, targetPath: MutableList<Int> = arrayListOf()): Boolean {
        if (this == target) return true
        else {
            widgets.map { it as Widget }.forEachIndexed { index, widget ->
                targetPath.add(index)
                if (widget == target) return true
                if (widget.getPathToNode(target, targetPath)) return true
                targetPath.removeLast()
            }
        }
        return false
    }

    fun getNodeAtPath(path: List<Int>): Widget? {
        var currentNode: Widget? = this
        for (index in path) {
            currentNode = currentNode?.widgets?.map { it as Widget }?.getOrNull(index)
            if (currentNode == null) return null
        }
        return currentNode
    }
}

@HollowPacketV2(HollowPacketV2.Direction.TO_CLIENT)
@Serializable
class OpenGuiPacket(val gui: Widget) : HollowPacketV3<OpenGuiPacket> {
    override fun handle(player: Player, data: OpenGuiPacket) {
        CURRENT_CLIENT_GUI = gui
        Minecraft.getInstance().setScreen(UIScreen(gui))
    }
}

fun main() {
    val gui = gui {
        size(90.pw, 90.pw)

        image("hc:textures/gui/background_ftbq.png") {
            align(Alignment.TOP_LEFT)
            size(10.px, 10.px)
            offset(mouse, mouse)
            zLayer = 1
        }

        image("hc:textures/gui/background_ftbq.png") {
            align(Alignment.LEFT_CENTER)
            size(100.pw, 100.pw)
        }

        button("Дать леща", "hc:textures/gui/icons/volume_slider.png") {
            align(Alignment.LEFT_CENTER)
            size(70.pw, 30.pw)

            onClick = { ServerLifecycleHooks.getCurrentServer().playerList.players.forEach(Player::sweepAttack) }
        }
        button("Сломать колени", "hc:textures/gui/icons/volume_slider.png") {
            align(Alignment.LEFT_CENTER)
            size(70.pw, 30.pw)
            offset(0.px, 30.pw + 10.px)

            onClick = { ServerLifecycleHooks.getCurrentServer().playerList.players.forEach(Player::kill) }
        }

        entity(ServerLifecycleHooks.getCurrentServer().playerList.players.first()) {
            enableScissors = true
            align(Alignment.TOP_RIGHT)
            size(40.pw, 100.pw)
            scale = 4f
            entityY = 50.px
        }
    }

    val nbt = NBTFormat.serialize<Widget>(gui)

    HollowCore.LOGGER.debug("nbt: {}", nbt)
    CURRENT_SERVER_GUI = gui
    OpenGuiPacket(gui).send(PacketDistributor.ALL.noArg())
}

var CURRENT_CLIENT_GUI: Widget? = null
var CURRENT_SERVER_GUI: Widget? = null