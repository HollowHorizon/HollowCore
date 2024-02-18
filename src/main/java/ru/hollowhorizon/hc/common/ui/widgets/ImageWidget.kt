package ru.hollowhorizon.hc.common.ui.widgets

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import kotlinx.serialization.Serializable
import net.minecraft.client.gui.screens.Screen
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import ru.hollowhorizon.hc.api.utils.Polymorphic
import ru.hollowhorizon.hc.client.utils.nbt.ForResourceLocation
import ru.hollowhorizon.hc.client.utils.rl
import ru.hollowhorizon.hc.common.ui.IWidget
import ru.hollowhorizon.hc.common.ui.Widget

@Serializable
@Polymorphic(IWidget::class)
class ImageWidget(val image: @Serializable(ForResourceLocation::class) ResourceLocation) : Widget() {
    @OnlyIn(Dist.CLIENT)
    override fun renderWidget(
        stack: PoseStack,
        x: Int,
        y: Int,
        screenWidth: Int,
        screenHeight: Int,
        widgetWidth: Int,
        widgetHeight: Int,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float,
    ) {
        RenderSystem.setShaderTexture(0, image)
        Screen.blit(stack, x, y, 0f, 0f, widgetWidth, widgetHeight, widgetWidth, widgetHeight)
    }
}

fun Widget.image(image: String, config: ImageWidget.() -> Unit = {}) { this += ImageWidget(image.rl).apply(config) }