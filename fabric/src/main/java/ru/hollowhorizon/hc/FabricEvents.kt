package ru.hollowhorizon.hc

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ConfigurationBuilder
import ru.hollowhorizon.hc.common.events.EventBus
import ru.hollowhorizon.hc.common.events.post
import ru.hollowhorizon.hc.common.events.registry.RegisterCommandsEvent
import ru.hollowhorizon.hc.common.events.registry.RegisterEntityAttributesEvent
import ru.hollowhorizon.hc.common.registry.getAnnotatedClasses
import ru.hollowhorizon.hc.common.registry.getAnnotatedMethods
import ru.hollowhorizon.hc.common.registry.getSubTypes

object FabricEvents {
    init {
        registerAttributes()
        registerCommands()

        getSubTypes = {
            Reflections(
                ConfigurationBuilder().forPackage("").addScanners(Scanners.SubTypes)
            ).getSubTypesOf(it)
        }
        getAnnotatedClasses = {
            Reflections(
                ConfigurationBuilder().forPackage("").addScanners(Scanners.TypesAnnotated)
            ).getTypesAnnotatedWith(it as Class<out Annotation>)
        }
        getAnnotatedMethods = {
            Reflections(
                ConfigurationBuilder().forPackage("").addScanners(Scanners.MethodsAnnotated)
            ).getMethodsAnnotatedWith(it as Class<out Annotation>)
        }
    }

    private fun registerAttributes() {
        val attributes = RegisterEntityAttributesEvent()
        EventBus.post(attributes)
        attributes.getAttributes().forEach(FabricDefaultAttributeRegistry::register)
    }

    private fun registerCommands() {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, registryAccess, environment ->
            RegisterCommandsEvent(dispatcher, registryAccess, environment).post()
        })
    }
}