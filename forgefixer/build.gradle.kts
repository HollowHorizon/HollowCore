plugins {
    id("java")
}

group = "ru.hollowhorizon.hc"
version = "1.0.0"

repositories {
    maven("https://maven.minecraftforge.net/")
    mavenCentral()
}

dependencies {
    implementation("net.minecraftforge:bootstrap-api:2.1.3")
}

tasks.jar {
    manifest {
        attributes("FMLModType" to "LIBRARY")
    }
}