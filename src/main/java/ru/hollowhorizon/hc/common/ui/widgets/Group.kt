package ru.hollowhorizon.hc.common.ui.widgets

import kotlinx.serialization.Serializable
import ru.hollowhorizon.hc.api.utils.Polymorphic
import ru.hollowhorizon.hc.common.ui.IWidget
import ru.hollowhorizon.hc.common.ui.Widget

@Serializable
@Polymorphic(IWidget::class)
class Group: Widget()

fun Widget.groupOf(vararg widgets: Widget) {
    this += Group().apply {

    }
}