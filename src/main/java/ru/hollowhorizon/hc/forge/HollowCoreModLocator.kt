//? if forge {
/*package ru.hollowhorizon.hc.forge

import net.minecraftforge.forgespi.locating.IModFile
import net.minecraftforge.forgespi.locating.IModLocator
import java.nio.file.Path
import java.util.function.Consumer

class HollowCoreModLocator: IModLocator {
    override fun name(): String {
        return "HollowCore Loader"
    }

    override fun scanFile(modFile: IModFile, pathConsumer: Consumer<Path>) {
        println("HollowCore scanFile")
    }

    override fun initArguments(arguments: MutableMap<String, *>) {
        println("HollowCore initArguments")
    }

    override fun isValid(modFile: IModFile): Boolean {
        return true
    }

    override fun scanMods(): MutableList<IModLocator.ModFileOrException> {
        return mutableListOf()
    }
}
*///?}