import net.fabricmc.loom.api.remapping.RemapperExtension
import net.fabricmc.loom.api.remapping.RemapperParameters
import net.fabricmc.loom.extension.LoomGradleExtensionImpl
import net.fabricmc.loom.extension.RemapperExtensionHolder
import net.fabricmc.tinyremapper.TinyRemapper

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val minecraft_version: String by project
val mod_name: String by project
val mod_id: String by project
val forge_version: String by project
val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating

loom {
    forge {
        convertAccessWideners = true
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
        mixinConfig("$mod_id.mixins.json")
        (loom as LoomGradleExtensionImpl).remapperExtensions.add(ForgeFixer)
    }
}

architectury {
    platformSetupLoomIde()
    forge()
}

configurations {
    compileClasspath.get().extendsFrom(common)
    runtimeClasspath.get().extendsFrom(common)
    named("developmentForge").get().extendsFrom(common)
}

configurations.configureEach {
    // Fix that can be found in Forge MDK too
    resolutionStrategy {
        force("net.sf.jopt-simple:jopt-simple:5.0.4")
    }
}

base {
    archivesName = "$mod_name-forge"
}

dependencies {
    forge("net.minecraftforge:forge:${minecraft_version}-${forge_version}")

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(path = ":common", configuration = "transformProductionForge")) { isTransitive = false }
}

tasks {
    shadowJar {
        configurations = listOf(shadowCommon)
        archiveClassifier = "dev-shadow"
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
        dependsOn(shadowJar.get())
        archiveClassifier.set(null as String?)
    }
}

object ForgeFixer : RemapperExtensionHolder(object : RemapperParameters {}) {
    override fun getRemapperExtensionClass(): Property<Class<out RemapperExtension<*>>> {
        throw UnsupportedOperationException("How did you call this method?")
    }

    override fun apply(
        tinyRemapperBuilder: TinyRemapper.Builder,
        sourceNamespace: String,
        targetNamespace: String,
        objectFactory: ObjectFactory,
    ) {
        // Under some strange circumstances there are errors with mapping source names, but that doesn't stop me from compiling the jar, does it?
        tinyRemapperBuilder.ignoreConflicts(true)
    }
}
