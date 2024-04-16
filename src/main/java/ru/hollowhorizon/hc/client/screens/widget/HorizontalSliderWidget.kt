/*
 * MIT License
 *
 * Copyright (c) 2024 HollowHorizon
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package ru.hollowhorizon.hc.client.screens.widget

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import ru.hollowhorizon.hc.client.screens.widget.layout.SmoothScrolling
import ru.hollowhorizon.hc.client.utils.math.Interpolation
import ru.hollowhorizon.hc.client.utils.mcText


class HorizontalSliderWidget(x: Int, y: Int, width: Int, height: Int, private val texture: ResourceLocation) :
    HollowWidget(x, y, width, height, "".mcText),
    IOriginBlackList {
    var maxWidth = 0
    var xWidth = 0
    private var isClicked = false
    private var consumer: (Float, Float) -> Unit = { _, _ -> }
    var smoothScrolling: SmoothScrolling? = null

    init {
        require(width >= height) { "Height must be less than width, it's a horizontal slider! Not a vertical one!" }
    }

    override fun init() {
        super.init()
        if (maxWidth == 0) {
            maxWidth = width - 20
            xWidth = x + 10
        }
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
        if (smoothScrolling?.update() == true) smoothScrolling = null
        super.renderButton(stack, mouseX, mouseY, ticks)
        if (isClicked) {
            val old = scroll
            xWidth = clamp(mouseX)
            consumer(old, (xWidth - x - 10) / (maxWidth + 0f))
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
            if (isClicked) return
            val old = scroll
            smoothScrolling = SmoothScrolling(
                startValue = old, endValue = modifier, duration = 5, interpolation = Interpolation.LINEAR
            ) { float ->
                xWidth = clamp(x + (maxWidth * float).toInt() + 10)
            }
            consumer.invoke(old, modifier)
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
