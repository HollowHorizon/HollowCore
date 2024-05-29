plugins {
    id("multiloader-loader")
    id("net.neoforged.gradle.userdev").version("7.0.133")
}

val minecraft_version: String by project
val mod_name: String by project
val mod_id: String by project
val neoforge_version: String by project
val imguiVersion: String by project

val at = file("src/main/resources/META-INF/accesstransformer.cfg")
if (at.exists()) minecraft.accessTransformers.file(at)

runs {
    configureEach {
        modSource(project.sourceSets.main.get())
    }
    create("client") {
        systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
    }
    create("server") {
        systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
        programArgument("--nogui")
    }

    create("gameTestServer") {
        systemProperty("neoforge.enabledGameTestNamespaces", mod_id)
    }

    create("data") {
        programArguments.addAll(
            "--mod", mod_id, "--all", "--output",
            file("src/generated/resources/").absolutePath,
            "--existing", file("src/main/resources/").absolutePath
        )
    }
}

sourceSets.main.get().resources { srcDir("src/generated/resources") }

dependencies {
    implementation("net.neoforged:neoforge:$neoforge_version")

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