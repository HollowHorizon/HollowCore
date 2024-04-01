package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ru.hollowhorizon.hc.client.screens.widget.layout.SmoothScrolling
import ru.hollowhorizon.hc.client.utils.math.Interpolation
import ru.hollowhorizon.hc.client.utils.mcText
import java.util.function.BiConsumer

class VerticalSliderWidget @JvmOverloads constructor(
    x: Int,
    y: Int,
    w: Int,
    h: Int,
    private val texture: ResourceLocation = ResourceLocation(
        "hc",
        "textures/gui/buttons/scrollbar.png"
    )
) : HollowWidget(x, y, w, h, "".mcText), IOriginBlackList {
    var maxHeight = 0
    var yHeight = 0
    private var isClicked = false
    private var consumer = { _: Float, _: Float -> }
    var smoothScrolling: SmoothScrolling? = null

    init {
        require(w <= h) { "Width must be less than height, it's a vertical slider! Not a horizontal one!" }
    }

    override fun setHeight(value: Int) {
        super.setHeight(value)
        init()
    }

    override fun init() {
        super.init()
        if (maxHeight == 0) {
            this.maxHeight = this.height - 20
            yHeight = this.y + 10
        }
    }

    fun clamp(value: Int): Int {
        return Mth.clamp(value, this.y + 10, this.y + this.height - 10)
    }

    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        if(smoothScrolling?.update() == true) smoothScrolling = null
        super.renderButton(stack, mouseX, mouseY, ticks)

        if (isClicked) {
            val old = scroll
            yHeight = clamp(mouseY)
            consumer(old, (yHeight - y - 10) / (maxHeight + 0f))
        }

        bind(texture)

        //render boarder
        blit(
            stack,
            this.x,
            this.y,
            (this.width * 2).toFloat(),
            0f,
            this.width,
            this.width,
            this.width * 3,
            this.width * 3
        )
        blit(
            stack,
            this.x,
            this.y + this.height - this.width,
            (this.width * 2).toFloat(),
            (this.width * 2).toFloat(),
            this.width,
            this.width,
            this.width * 3,
            this.width * 3
        )
        blit(
            stack,
            this.x,
            this.y + this.width,
            (this.width * 2).toFloat(),
            (this.height - this.width * 2).toFloat(),
            this.width,
            this.height - this.width * 2,
            this.width * 3,
            (this.height - this.width * 2) * 3
        )

        //render scroll
        if (mouseY > this.yHeight - 10 && mouseY < this.yHeight + 10 && mouseX > this.x && mouseX < this.x + this.width || isClicked) blit(
            stack, this.x, this.yHeight - 10,
            width.toFloat(), 0f, this.width, 20, this.width * 3, 20
        )
        else blit(stack, this.x, this.yHeight - 10, 0f, 0f, this.width, 20, this.width * 3, 20)
    }

    var scroll: Float
        get() = (yHeight - y - 10) / (maxHeight + 0f)
        set(modifier) {
            if(isClicked) return
            val old = scroll
            smoothScrolling = SmoothScrolling(
                startValue = old, endValue = modifier, duration = 5, interpolation = Interpolation.LINEAR
            ) { float ->
                yHeight = clamp(y + (maxHeight * float).toInt() + 10)
            }
            this.consumer(old, modifier)
        }

    fun onValueChange(consumer: (Float, Float) -> Unit) {
        this.consumer = consumer
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0 && isHovered(mouseX, mouseY)) {
            isClicked = true
            return true
        }
        return false
    }

    override fun mouseReleased(mouseX: Double, mouseY: Double, button: Int): Boolean {
        if (button == 0) {
            isClicked = false
            return true
        }
        return false
    }
}
