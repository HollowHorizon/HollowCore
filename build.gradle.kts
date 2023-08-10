import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
//^(.+)$(?=[\s\S]*^(\1)$[\s\S]*)
buildscript {
    repositories {
        maven { url = uri("https://maven.minecraftforge.net") }
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
        maven { url = uri("https://maven.parchmentmc.org") }
        mavenCentral()
    }
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:5+") { isChanging = true }
        classpath("org.parchmentmc:librarian:1.+")
        classpath("org.spongepowered:mixingradle:0.7.32")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0-RC")
        classpath("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7+")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm").version("1.9.0-RC")
    id("org.jetbrains.kotlin.plugin.serialization").version("1.9.0-RC")
}

apply {
    plugin("kotlin")
    plugin("maven-publish")
    plugin("com.github.johnrengelman.shadow")
    plugin("net.minecraftforge.gradle")
    plugin("org.spongepowered.mixin")
    plugin("org.parchmentmc.librarian.forgegradle")
}

group = "ru.hollowhorizon"
version = "1.1.0"
project.setProperty("archivesBaseName", "hc")

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all", "-Xopt-in=kotlin.RequiresOptIn")
}

configure<UserDevExtension> {
    //copyIdeResources.set(true)

    mappings("parchment", "2022.11.27-1.19.2")

    accessTransformer("src/main/resources/META-INF/accesstransformer.cfg")

    runs.create("client") {
        workingDirectory(project.file("run"))
        property("forge.logging.markers", "REGISTRIES") // eg: SCAN,REGISTRIES,REGISTRYDUMP
        property("forge.logging.console.level", "debug")
        jvmArg("-XX:+AllowEnhancedClassRedefinition")
        arg("-mixin.config=hc.mixins.json")
        mods.create("hc") {
            source(the<JavaPluginExtension>().sourceSets.getByName("main"))
        }
    }

    runs.create("server") {
        workingDirectory(project.file("run"))
        property("forge.logging.markers", "REGISTRIES") // eg: SCAN,REGISTRIES,REGISTRYDUMP
        property("forge.logging.console.level", "debug")
        arg("-mixin.config=hc.mixins.json")
        jvmArg("-XX:+AllowEnhancedClassRedefinition")
        mods.create("hc") {
            source(the<JavaPluginExtension>().sourceSets.getByName("main"))
        }
    }

    runs.all {
        lazyToken("minecraft_classpath") {
            configurations["library"].copyRecursive().resolve().joinToString(File.pathSeparator) { it.absolutePath }
        }
    }
}


repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    flatDir {
        dirs("libs")
        dirs("build/libs")
    }
}

val shadowCompileOnly by configurations.creating

dependencies {
    val minecraft = configurations["minecraft"]
    val shadow = configurations["shadow"]

    minecraft("net.minecraftforge:forge:1.19.2-43.2.21")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    shadow("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0-RC")
    shadow("org.jetbrains.kotlin:kotlin-stdlib-common:1.9.0-RC")

    shadow("org.jetbrains.kotlin:kotlin-scripting-jvm-host:1.9.0-RC")
    shadow("org.jetbrains.kotlin:kotlin-scripting-jvm:1.9.0-RC")

    shadow("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:1.9.0-RC")
    shadowCompileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.0-RC")

    shadow("org.jetbrains.kotlin:kotlin-script-runtime:1.9.0-RC")

    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    shadow("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")
    shadow("com.esotericsoftware:kryo:5.4.0")

    shadow("gnu.trove:trove:1.0.2")
}

val copyJar by tasks.registering(Copy::class) {
    from("build/libs/hc-1.1.0.jar")
    into("C:\\Users\\user\\Twitch\\Minecraft\\Instances\\Instances\\test1\\mods")
}



val library = configurations.create("library")

configurations {
    //library - 3rd-party library (not a mod)
    //shade - dep that should be shaded
    implementation.get().extendsFrom(library)
    library.extendsFrom(this["shadow"])

    compileOnly.get().extendsFrom(shadowCompileOnly)
}

tasks.getByName("build").dependsOn("shadowJar")
tasks.getByName("shadowJar").finalizedBy("copyJar")

tasks.getByName<ShadowJar>("shadowJar") {
    //minimize()

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    configurations = listOf(project.configurations.getByName("shadow"), project.configurations.getByName("shadowCompileOnly"))

    val shadowPackages = listOf(
        "gnu.trove"
    )

    shadowPackages.forEach {
        relocate(it, "ru.hollowhorizon.repack.$it")
    }

    //Нужно переименовать все пакеты с названием "native" в "notnative" потому что JDK отказывается загружать пакеты с такими именами
    relocate(HollowRelocator("native", "notnative", ArrayList<String>(), ArrayList<String>(), true))
//    exclude {
//        val res = it.relativePath.contains("native") && it.relativePath.contains("org.jetbrains")
//        project.logger.info("Testing: $it")
//        res
//    }

    archiveClassifier.set("")

    mergeServiceFiles()
    exclude("**/module-info.class")

    finalizedBy("reobfShadowJar")
}

(extensions["reobf"] as NamedDomainObjectContainer<*>).create("shadowJar")

tasks.getByName<Jar>("jar") {
    archiveClassifier.set("original")

    manifest {
        attributes(
            mapOf(
                "Specification-Title" to "HollowCore",
                "Specification-Vendor" to "HollowHorizon",
                "Specification-Version" to "1", // We are version 1 of ourselves
                "Implementation-Title" to project.name,
                "Implementation-Version" to version,
                "Implementation-Vendor" to "HollowHorizon",
                "Implementation-Timestamp" to ZonedDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")),
                "MixinConfigs" to "hc.mixins.json"
            )
        )
    }

    finalizedBy("reobfJar")
}

