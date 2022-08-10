import com.mojang.blaze3d.matrix.MatrixStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screen.Screen
import net.minecraft.util.text.StringTextComponent

class GUI : Screen(StringTextComponent("SCREEN")) {
    override fun render(p_230430_1_: MatrixStack, p_230430_2_: Int, p_230430_3_: Int, p_230430_4_: Float) {
        font.drawShadow(p_230430_1_, "Hello World!", width / 2F - font.width("Hello World!") / 2F, height / 2F, 0xFFFFFF)
    }
}

Minecraft.getInstance().setScreen(GUI())