import ru.hollowhorizon.hc.client.screens.widget.ImageWidget
import ru.hollowhorizon.hc.client.utils.parent
import ru.hollowhorizon.hc.client.utils.toRL

println("loading image")

ImageWidget.ofResource(
    "hc:textures/gui/background_ftbq.png".toRL(),
    width / 2 - 100, height / 2 - 100, 200, 200
).onMouseClicked { mouseX, mouseY, button ->
    println("$mouseX $mouseY $button")
} parent screen

println("done")