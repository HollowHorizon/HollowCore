package ru.hollowhorizon.hc.core

import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import ru.hollowhorizon.hc.common.registry.HollowModProcessor
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
                    .collect(Collectors.toMap(HollowModTarget::modId, Function.identity()) { a, _ -> a })
            )
        }
    }

    override fun <R : ILifecycleEvent<R>?> consumeLifecycleEvent(consumeEvent: Supplier<R>?) {}

    class HollowModTarget(private val className: String, val modId: String) : IModLanguageProvider.IModLanguageLoader {
        init {
            println("Loading HollowMod $modId")
        }

        @Suppress("UNCHECKED_CAST")
        override fun <T> loadMod(info: IModInfo, modClassLoader: ClassLoader, modFileScanResults: ModFileScanData): T {

            val fmlContainer = Class.forName(
                "ru.hollowhorizon.hc.core.HollowModContainer",
                true,
                Thread.currentThread().contextClassLoader
            )

            val constructor = fmlContainer.declaredConstructors[0]

            return constructor.newInstance(info, className, modClassLoader, modFileScanResults) as T
        }

    }
}