package assets.hc.screen

import net.minecraft.client.gui.widget.TextFieldWidget
import ru.hollowhorizon.hc.client.screens.HollowScreen
import ru.hollowhorizon.hc.client.screens.util.Alignment
import ru.hollowhorizon.hc.client.screens.widget.layout.box
import ru.hollowhorizon.hc.client.utils.toSTC

class GuiTest : HollowScreen("".toSTC()) {
    override fun init() {
        super.init()

        box("Основное") {
            size = 90f x 90f
            align = Alignment.CENTER

            box("Левая часть") {
                size = 50f x 50f
                align = Alignment.LEFT_CENTER

                elements {
                    +TextFieldWidget(font, x0, y0, width, height, "Текстовое поле".toSTC())
                }
            }

            box("Правая часть") {
                size = 50f x 50f
                align = Alignment.RIGHT_CENTER
            }
        }
    }
}