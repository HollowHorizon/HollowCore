package ru.hollowhorizon.hc.common.registry

import ru.hollowhorizon.hc.LOGGER

/**
 * Possibility to select automatic model generation.
 */
enum class AutoModelType(
    @set:JvmName("_setModelId")
    var modelId: String
) {
    DEFAULT("item/generated"),
    HANDHELD("item/handheld"),
    CUSTOM("");

    fun setModelId(model: String): AutoModelType {
        if (this == CUSTOM)
            this.modelId = model
        else LOGGER.warn("Custom model id can be set only if type == CUSTOM")

        return this
    }
}
