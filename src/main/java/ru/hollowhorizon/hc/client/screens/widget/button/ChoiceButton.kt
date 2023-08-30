package ru.hollowhorizon.hc.client.screens.widget.button

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.texture.TextureManager
import net.minecraft.client.resources.sounds.SimpleSoundInstance
import net.minecraft.client.sounds.SoundManager
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import javax.annotation.Nonnull

class ChoiceButton(x: Int, y: Int, width: Int, height: Int, text: Component, onPress: BaseButton.() -> Unit) :
    BaseButton(x, y, width, height, text, onPress, TextureManager.INTENTIONAL_MISSING_TEXTURE) {
    override fun playDownSound(soundHandler: SoundManager) {
        soundHandler.play(
            SimpleSoundInstance.forUI(
                ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation("hc:choice_button"))!!,
                1f
            )
        )
    }

    override fun render(@Nonnull stack: PoseStack, x: Int, y: Int, f: Float) {
        val minecraft = Minecraft.getInstance()
        val fr = minecraft.font
        stack.pushPose()
        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        bind(ResourceLocation("hc", "textures/gui/lore/button_1.png"))
        blit(
            stack,
            this.x,
            this.y,
            0f,
            (if (isCursorAtButton(x, y)) height else 0).toFloat(),
            16,
            height,
            16,
            height * 2
        )
        bind(ResourceLocation("hc", "textures/gui/lore/button_3.png"))
        blit(
            stack,
            this.x + 16,
            this.y,
            0f,
            (if (isCursorAtButton(x, y)) height else 0).toFloat(),
            width - 32,
            height,
            width - 32,
            height * 2
        )
        bind(ResourceLocation("hc", "textures/gui/lore/button_2.png"))
        blit(
            stack,
            this.x + width - 16,
            this.y,
            0f,
            (if (isCursorAtButton(x, y)) height else 0).toFloat(),
            16,
            height,
            16,
            height * 2
        )
        stack.translate(0.0, 0.0, 120.0)
        fr.draw(stack, message, this.x + width / 2f - fr.width(message) / 2f, this.y + height / 4f, 0xFFFFFF)
        stack.popPose()
    }

    override fun isCursorAtButton(cursorX: Int, cursorY: Int): Boolean {
        return cursorX >= x && cursorY >= y && cursorX <= x + width && cursorY <= y + height
    }
}
