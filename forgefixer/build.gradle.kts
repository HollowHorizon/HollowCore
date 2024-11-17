plugins {
    id("java")
}

group = "ru.hollowhorizon.hc"
version = "1.0.0"

repositories {
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://maven.0mods.team/releases")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://maven.parchmentmc.org")
    maven("https://maven.blamejared.com")
    maven("https://maven.shedaniel.me/")
    maven("https://maven.architectury.dev/")
    maven("https://maven.terraformersmc.com/releases/")
    maven("https://maven.0mods.team/releases")
    maven("https://jitpack.io")
    maven("https://maven.neoforged.net/releases")
    maven("https://maven.fabricmc.net/")
    maven("https://maven.cleanroommc.com")
    maven("https://cursemaven.com")

    mavenCentral()
}

dependencies {
    implementation("cpw.mods:modlauncher:10.0.10")
    implementation("net.minecraftforge:fmlloader:1.20.1-47.3.6")
}

tasks.jar {
    manifest {
        attributes("FMLModType" to "LIBRARY")
    }
}