//? if forge {
/*package ru.hollowhorizon.hc.forge

import net.minecraftforge.fml.loading.FMLLoader
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo
import ru.hollowhorizon.hc.client.utils.ModList
import java.io.File

object ForgeModList : ModList {
    val forgeList = net.minecraftforge.fml.ModList.get()
    override fun isLoaded(modId: String): Boolean {
        return forgeList.isLoaded(modId)
    }

    override fun getFile(modId: String): File {
        val file = forgeList.getModFileById(modId)
        (file as ModFileInfo).file.secureJar.primaryPath
        return null as File
    }

    override val mods: List<String>
        get() = emptyList() //forgeList.mods.map { it.namespace }
}
*///?}