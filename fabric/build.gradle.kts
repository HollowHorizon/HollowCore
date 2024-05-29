@file:Suppress("UnstableApiUsage")

val minecraft_version: String by project
val mod_id: String by project
val fabric_loader_version: String by project
val fabric_version: String by project
val imguiVersion: String by project

plugins {
    id("multiloader-loader")
    id("fabric-loom").version("1.6-SNAPSHOT")
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings(loom.officialMojangMappings())

    modImplementation("net.fabricmc:fabric-language-kotlin:1.10.18+kotlin.1.9.22")
    modImplementation("net.fabricmc:fabric-loader:$fabric_loader_version")
    modImplementation("net.fabricmc.fabric-api:fabric-api:$fabric_version")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.22")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.ow2.asm:asm:9.7")
    implementation("org.reflections:reflections:0.10.2")

    implementation("io.github.spair:imgui-java-binding:$imguiVersion")
    implementation("io.github.spair:imgui-java-lwjgl3:$imguiVersion")
    implementation("io.github.spair:imgui-java-natives-windows:$imguiVersion")
    implementation("io.github.spair:imgui-java-natives-linux:$imguiVersion")
    implementation("io.github.spair:imgui-java-natives-macos:$imguiVersion")
}

loom {
    val aw = project(":common").file("src/main/resources/$mod_id.accesswidener")
    if (aw.exists()) accessWidenerPath.set(aw)

    mixin {
        defaultRefmapName.set("${mod_id}.refmap.json")
    }
    runs {
        named("client") {
            client()
            configName = "Fabric Client"
            ideConfigGenerated(true)
            runDir("runs/client")
        }
        named("server") {
            server()
            configName = "Fabric Server"
            ideConfigGenerated(true)
            runDir("runs/server")
        }
    }
}