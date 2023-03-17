package ru.hollowhorizon.hc.client.screens

import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.gui.widget.button.Button
import ru.hollowhorizon.hc.client.screens.util.Alignment
import ru.hollowhorizon.hc.client.screens.widget.layout.PlacementType
import ru.hollowhorizon.hc.client.screens.widget.layout.WidgetBuilder
import ru.hollowhorizon.hc.client.screens.widget.layout.box
import ru.hollowhorizon.hc.client.utils.mcText
import ru.hollowhorizon.hc.client.utils.toSTC
import ru.hollowhorizon.hc.client.utils.use


class UIScreen : HollowScreen("".toSTC()) {
    override fun init() {
        super.init()

        box {
            size = 90.pc x 90.pc

            renderer = { stack, x, y, w, h ->
                stack.use {
                    fillGradient(stack, x, y, x + w, y + h, 0x4287F5FF, 0x224C8FFF)
                }
            }

            elements {
                box {
                    pos = 0.px x (-5).pc
                    size = 50.pc x 60.pc

                    padding = 1.pc x 1.pc

                    alignElements = Alignment.CENTER

                    placementType = PlacementType.GRID

                    elements {
                        for (i in 0..8) {
                            box {
                                size = 33.pc x 33.pc
                                align = Alignment.values()[i]
                                alignElements = Alignment.values()[i]
                                placementType = PlacementType.GRID
                                padding = 5.px x 5.px
                                spacing = 5.px x 5.px

                                renderer = { stack, x, y, w, h ->
                                    stack.use {
                                        fillGradient(stack, x, y, x + w, y + h, 0xfcad03FF.toInt(), 0x78746dFF)
                                    }
                                }

                                elements {
                                    for (j in 1..4) {
                                        //x, y - вычисляются автоматически, вместо них можно указать что угодно
                                        +Button(0, 0, 45.pc.w().value, 45.pc.h().value, "B$j".mcText) {}
                                    }
                                }
                            }
                        }
                    }
                }
                box {
                    pos = 0.px x (-5).pc
                    size = 25.pc x 60.pc
                    align = Alignment.LEFT_CENTER
                    padding = 5.px x 5.px
                    spacing = 5.px x 5.px
                    alignElements = Alignment.LEFT_CENTER
                    placementType = PlacementType.GRID

                    elements {
                        +Button(0, 0, 90.pc.w().value, 20.pc.h().value, "Кынопка 1".mcText) {}
                        +Button(0, 0, 90.pc.w().value, 20.pc.h().value, "Кпонка 3".mcText) {}
                        +Button(0, 0, 90.pc.w().value, 20.pc.h().value, "Кнопука 4".mcText) {}
                        +Button(0, 0, 90.pc.w().value, 20.pc.h().value, "Кнапка 5".mcText) {}
                        +Button(0, 0, 90.pc.w().value, 20.pc.h().value, "Кнопка 6".mcText) {}
                        +Button(0, 0, 90.pc.w().value, 20.pc.h().value, "Кнопка 7".mcText) {}
                    }
                }
                box {
                    align = Alignment.BOTTOM_CENTER
                    size = 100.pc x 20.pc

                    padding = 5.pc x 5.pc

                    elements {
                        box {
                            align = Alignment.BOTTOM_RIGHT

                            val ySize = 100.pc.apply { isWidth = false }

                            size = 100.pc.w() - 100.pc.h() x ySize
                        }
                        box {
                            align = Alignment.BOTTOM_LEFT

                            size = 100.pc.h() x 100.pc.h()
                        }
                    }
                }
                box {

                    align = Alignment.TOP_CENTER
                    size = 100.pc x 10.pc

                    padding = 10.pc x 5.px

                    spacing = 10.px x 5.px
                    placementType = PlacementType.HORIZONTAL

                    elements {
                        for(i in 0..10) {
                            +TextFieldWidget(font, 0, 0, 90.pc.h().value * 4, 90.pc.h().value, "T${i}".mcText)
                        }
                    }
                }
            }
        }
    }
}