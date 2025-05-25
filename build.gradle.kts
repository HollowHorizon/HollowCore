plugins {
    java
    `maven-publish`
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("me.fallenbreath.yamlang")
    kotlin("jvm")
    kotlin("plugin.serialization")
}

val koolVersion: String by properties
val modId: String by properties
val modName: String by properties
val modVersion: String by properties
val license: String by properties

val container = ModContainer(
    minecraftVersion = stonecutter.current.project.substringBeforeLast('-'),
    modPlatform = stonecutter.current.project.substringAfterLast('-'),
    modId = modId, modName = modName, license = license, modVersion = modVersion,
)

val kotlinVersion: String by properties

group = properties["mod_group"].toString()
version = modVersion
base.archivesName = "$modName-${container.modPlatform}-${container.minecraftVersion}"

setupEnviroment(container, kotlinVersion, "TheHollowHorizon", includeKotlin = true, enablePublishing = true)

dependencies {
    // CONFIG //
    install("com.akuleshov7:ktoml-core-jvm:0.5.1")
    install("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.8.1")

    // GRAPHICS //
    install("de.fabmax.kool:kool-core-desktop:$koolVersion")
    install("org.codehaus.janino:janino:3.1.12")
}

kotlin.compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")