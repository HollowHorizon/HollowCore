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


val container = ModProject(
    modId = modId,
    modName = modName,
    modVersion = modVersion,
    license = license,

    entryPoints = mapOf(
        "main" to listOf("ru.hollowhorizon.hc.fabric.HCFabric::onCommonInitialize"),
        "client" to listOf("ru.hollowhorizon.hc.fabric.HCFabric::onClientInitialize")
    ),
    dependencies = mapOf(),

    username = "TheHollowHorizon"
)

val kotlinVersion: String by properties
val publications = ArrayList<Publication>()

if (System.getenv("MAVEN_PASSWORD_ZM") != null) publications.add(
    Publication(
        "ZeroModsMaven",
        "https://maven.0mods.team/releases",
        System.getenv("MAVEN_USER_ZM"),
        System.getenv("MAVEN_PASSWORD_ZM")
    )
)

setupEnviroment(container, kotlinVersion, includeKotlin = true, *publications.toTypedArray())

dependencies {
    // CONFIG //
    install("com.akuleshov7:ktoml-core-jvm:0.5.1")
    install("org.jetbrains.kotlinx:kotlinx-serialization-cbor:1.8.1")

    // GRAPHICS //
    install("de.fabmax.kool:kool-core-desktop:$koolVersion")
}

kotlin.compilerOptions.freeCompilerArgs.add("-Xjvm-default=all")
