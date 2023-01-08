package ru.hollowhorizon.hc.common.scripting

import kotlin.script.experimental.annotations.KotlinScript


@KotlinScript(
    fileExtension = "hs.kts",
    compilationConfiguration = HollowScriptConfiguration::class
)
abstract class HollowScript {
}