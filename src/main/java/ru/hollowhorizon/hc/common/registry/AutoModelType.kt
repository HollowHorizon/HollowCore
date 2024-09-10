package ru.hollowhorizon.hc.common.registry

/**
 *
 */
enum class AutoModelType(
    @set:JvmName("_setModelId")
    var modelId: String
) {
    DEFAULT("item/generated"),
    HANDHELD("item/handheld"),
    CUSTOM("");

    fun setModelId(model: String): AutoModelType {
        this.modelId = model
        return this
    }
}
