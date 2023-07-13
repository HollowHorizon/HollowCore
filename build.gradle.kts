import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

buildscript {
    repositories {
        maven { url = uri("https://maven.minecraftforge.net") }
        maven { url = uri("https://repo.spongepowered.org/repository/maven-public/") }
        maven { url = uri("https://maven.parchmentmc.org") }
        mavenCentral()
    }
    dependencies {
        classpath("net.minecraftforge.gradle:ForgeGradle:5.1+") { isChanging = true }
        classpath("org.parchmentmc:librarian:1.+")
        classpath("org.spongepowered:mixingradle:0.7.32")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0-RC")
        classpath("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7.0.0")
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
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

configurations {
    implementation.get().extendsFrom(this["shadow"])
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all", "-Xopt-in=kotlin.RequiresOptIn")
}

configure<UserDevExtension> {
    mappings("parchment", "2022.03.06-1.16.5")

    accessTransformer("src/main/resources/META-INF/accesstransformer.cfg")

    runs.create("client") {
        workingDirectory(project.file("run"))
        property("forge.logging.markers", "REGISTRIES") // eg: SCAN,REGISTRIES,REGISTRYDUMP
        property("forge.logging.console.level", "debug")
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
        mods.create("hc") {
            source(the<JavaPluginExtension>().sourceSets.getByName("main"))
        }
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    flatDir { dirs("libs") }
}

dependencies {
    val minecraft = configurations["minecraft"]
    val shadow = configurations["shadow"]

    minecraft("net.minecraftforge:forge:1.16.5-36.2.39")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    shadow("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.0-RC")

    shadow("org.jetbrains.kotlin:kotlin-scripting-jvm-host:1.9.0-RC")
    shadow("org.jetbrains.kotlin:kotlin-scripting-jvm:1.9.0-RC")

    shadow("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:1.9.0-RC")
    shadow("org.jetbrains.kotlin:kotlin-scripting-compiler-impl-embeddable:1.9.0-RC")
    shadow("org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.0-RC")
    shadow("org.jetbrains.kotlin:kotlin-script-runtime:1.9.0-RC")

    shadow("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    shadow("org.jetbrains.kotlinx:kotlinx-serialization-core:1.5.0")

    shadow("gnu.trove:trove:1.0.2")
}

tasks.getByName("build").dependsOn("shadowJar")

tasks.getByName<ShadowJar>("shadowJar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    configurations = listOf(project.configurations.getByName("shadow"))

    val shadowPackages = listOf(
        "gnu.trove"
    )

    shadowPackages.forEach {
        relocate(it, "ru.hollowhorizon.repack.$it")
    }

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
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ"))
            )
        )
    }

    finalizedBy("reobfJar")
}