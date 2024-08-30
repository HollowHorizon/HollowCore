package ru.hollowhorizon.hc.fabric

//? if fabric {
/*import ru.hollowhorizon.hc.HollowCore
import ru.hollowhorizon.hc.client.utils.ModList
import ru.hollowhorizon.hc.common.scripting.kotlin.deobfClassPath
import ru.hollowhorizon.hc.common.scripting.mappings.Remapper

object GameRemapper {
    fun remap() {
        Remapper.remap(
            Remapper.DEOBFUSCATE_REMAPPER,
            ModList.INSTANCE.mods
                .filter { it in HollowCore.config.scripting.includeMods }
                .map { ModList.INSTANCE.getFile(it) }
                .filter { it.name.endsWith(".jar") }
                .toTypedArray(),
            deobfClassPath.toPath()
        )
    }
}
*///?}