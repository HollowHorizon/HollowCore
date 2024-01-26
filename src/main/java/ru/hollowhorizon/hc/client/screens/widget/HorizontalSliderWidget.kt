package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ru.hollowhorizon.hc.client.utils.mcText


class HorizontalSliderWidget(x: Int, y: Int, width: Int, height: Int, private val texture: ResourceLocation) :
    HollowWidget(x, y, width, height, "".mcText),
    IOriginBlackList {
    private var maxWidth = 0
    private var xWidth = 0
    private var isClicked = false
    private var consumer: (Float) -> Unit = {}

    init {
        require(width >= height) { "Height must be less than width, it's a horizontal slider! Not a vertical one!" }
    }

    override fun init() {
        super.init()
        maxWidth = width - 20
        xWidth = x + 10
    }

    override fun setWidth(p_230991_1_: Int) {
        super.setWidth(p_230991_1_)
        init()
    }

    constructor(x: Int, y: Int, w: Int, h: Int) : this(
        x,
        y,
        w,
        h,
        ResourceLocation("hc", "textures/gui/buttons/scrollbar_horizontal.png")
    )

    fun clamp(value: Int): Int {
        return Mth.clamp(value, x + 10, x + width - 10)
    }

    override fun renderButton(stack: PoseStack, mouseX: Int, mouseY: Int, ticks: Float) {
        super.renderButton(stack, mouseX, mouseY, ticks)
        if (isClicked) {
            xWidth = clamp(mouseX)
            consumer.invoke(scroll)
        }
        bind(texture)

        //render boarder
        blit(
            stack,
            x, y, 0f, (height * 2).toFloat(), height, height, height * 3, height * 3
        )
        blit(
            stack, x + width - height,
            y, (height * 2).toFloat(), (height * 2).toFloat(), height, height, height * 3, height * 3
        )
        blit(
            stack,
            x + height,
            y,
            width - height * 2f,
            height * 2f,
            width - height * 2,
            height,
            (width - height * 2) * 3,
            height * 3
        )

        //render scroll
        if (mouseX > xWidth - 10 && mouseX < xWidth + 10 && mouseY > y && mouseY < y + height || isClicked) blit(
            stack, xWidth - 10,
            y, 0f, height.toFloat(), 20,
            height, 20, height * 3
        ) else blit(
            stack, xWidth - 10,
            y, 0f, 0f, 20, height, 20, height * 3
        )
    }

    var scroll: Float
        get() = (xWidth - x - 10) / (maxWidth + 0f)
        set(modifier) {
            xWidth = clamp(x + (maxWidth * modifier).toInt() + 10)
            consumer.invoke(scroll)
        }

    fun onValueChange(consumer: (Float) -> Unit) {
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
