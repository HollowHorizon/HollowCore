package ru.hollowhorizon.hc.client.utils

import java.io.File

interface ModList {
    fun isLoaded(modId: String): Boolean
    fun getFile(modId: String): File

    val mods: List<String>

    companion object {
        lateinit var INSTANCE: ModList
    }
}