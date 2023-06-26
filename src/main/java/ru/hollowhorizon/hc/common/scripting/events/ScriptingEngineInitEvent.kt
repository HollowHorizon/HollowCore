package ru.hollowhorizon.hc.common.scripting.events

import net.minecraftforge.eventbus.api.Event
import java.io.File

class ScriptingEngineInitEvent : Event() {
    val scriptFolders = HashSet<File>()

    fun addFolder(file: File) = scriptFolders.add(file)

}

class OnScriptingEngineLoadEvent : Event()