package ru.hollowhorizon.hc.core

import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.javafmlmod.FMLModContainer
import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

class HollowModProvider : IModLanguageProvider {
    override fun name(): String {
        return "hollowforge"
    }

    override fun getFileVisitor(): Consumer<ModFileScanData> {
        return Consumer { scan ->
            scan.addLanguageLoader(
                scan.annotations.stream()
                    .filter {
                        it.annotationType.toString() == "Lru/hollowhorizon/hc/api/registy/HollowMod;"
                    }
                    .map { HollowModTarget(it.classType.className, it.annotationData["value"] as String) }
                    .collect(Collectors.toMap(HollowModTarget::modId, Function.identity()) { a, b -> a })
            )
        }
    }

    override fun <R : ILifecycleEvent<R>?> consumeLifecycleEvent(consumeEvent: Supplier<R>?) {}

    class HollowModTarget(private val className: String, val modId: String) : IModLanguageProvider.IModLanguageLoader {
        init {
            println("Loading HollowMod $modId")
        }

        override fun <T> loadMod(info: IModInfo, modClassLoader: ClassLoader, modFileScanResults: ModFileScanData): T {
            val fmlContainer = Class.forName(
                "net.minecraftforge.fml.javafmlmod.FMLModContainer",
                true,
                Thread.currentThread().contextClassLoader
            )

            val constructor = fmlContainer.declaredConstructors[0]

            return constructor.newInstance(info, className, modClassLoader, modFileScanResults) as T
        }

    }
}