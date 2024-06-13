@file:Suppress("UnstableApiUsage")

val minecraft_version: String by project
val mod_id: String by project
val fabric_loader_version: String by project
val fabric_version: String by project
val imguiVersion: String by project
val common: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val shadowBundle: Configuration by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

architectury {
    platformSetupLoomIde()
    fabric()
}

base {
    archivesName.set("${archivesName.get()}-fabric")
}

repositories {
    flatDir { dirs("libs") }
}

configurations {
    compileClasspath { extendsFrom(common) }
    runtimeClasspath { extendsFrom(common) }
    named("developmentFabric") { extendsFrom(common) }
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version") { include(this) }
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version") { include(this) }

    modImplementation("mods:sodium-fabric:0.5.8+mc1.20.6")
    modImplementation("mods:iris:1.7.0+mc1.20.6")

    common(project(path = ":common", configuration = "namedElements")) { isTransitive = false }
    shadowBundle(project(path = ":common", configuration = "transformProductionFabric"))
}

tasks {
    shadowJar {
        configurations = listOf(shadowBundle)
        archiveClassifier = "dev-shadow"
        relocate("team._0mods.aeternus", "team._0mods.aeternus.fabric")
    }

    remapJar {
        inputFile.set(shadowJar.get().archiveFile)
    }
}
