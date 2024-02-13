package ru.hollowhorizon.hc.common.capabilities

import dev.ftb.mods.ftbteams.data.TeamBase
import net.minecraft.world.entity.player.Player
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fml.Logging
import net.minecraftforge.forgespi.language.ModFileScanData
import org.objectweb.asm.Type
import ru.hollowhorizon.hc.HollowCore
import kotlin.reflect.KClass


@Target(AnnotationTarget.CLASS)
annotation class HollowCapabilityV2(vararg val value: KClass<*>) {
    @Suppress("UNCHECKED_CAST")
    companion object {

        fun <T> get(clazz: Class<T>): Capability<T> {
            return CapabilityStorage.storages[clazz.name] as Capability<T>
        }

        @JvmField
        val TYPE = Type.getType(HollowCapabilityV2::class.java)

    }
}

@Suppress("UNCHECKED_CAST")
fun <T> callHook(list: MutableList<ModFileScanData>, getMethod: (String, Boolean) -> Capability<T>) {
    val data = list.flatMap { it.annotations }
    val annotations = data
        .filter { HollowCapabilityV2.TYPE.equals(it.annotationType) }
        .distinct()
        .sortedBy { it.clazz.toString() }

    for (annotation in annotations) {

        HollowCore.LOGGER.debug(Logging.CAPABILITIES, "Attempting to automatically register: {}", annotation)
        val result = getMethod(
            annotation.clazz.internalName
                .replace("/", ".")
                .replace("$", "."), true
        )

        val targets: List<Type> =
            (annotation.annotationData["value"] as ArrayList<Type>)
        initCapabilities(Class.forName(annotation.clazz.className), result, targets)
    }
}

fun initCapabilities(capabilityClass: Class<*>, cap: Capability<*>, targets: List<Type>) {
    CapabilityStorage.storages[cap.name] = cap

    targets.forEach { target ->
        val targetClass = Class.forName(target.className)
        if (targetClass == Player::class.java) CapabilityStorage.playerCapabilities.add(cap)
        if (targetClass == TeamBase::class.java) CapabilityStorage.teamCapabilities.add(cap)

        CapabilityStorage.providers.add(targetClass to { provider ->
            (capabilityClass.getDeclaredConstructor().newInstance() as CapabilityInstance).apply {
                this.provider = provider
                this.capability = cap as Capability<CapabilityInstance>
            }
        })
    }
}
