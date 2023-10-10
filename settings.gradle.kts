pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
        maven { url = uri("https://maven.minecraftforge.net/") }
        maven { url = uri("https://maven.parchmentmc.org") }
    }
    plugins {
        id("net.minecraftforge.gradle") version "[6.0,6.2)"
        id("org.parchmentmc.librarian.forgegradle") version "1.+"
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

rootProject.name = "HollowCore"