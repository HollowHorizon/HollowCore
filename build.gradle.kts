import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import net.minecraftforge.gradle.common.util.RunConfig
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.ir.backend.js.compile
import org.spongepowered.asm.gradle.plugins.MixinExtension
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

val minecraft_version: String by project
val forge_version: String by project
val mod_id: String by project
val mod_group: String by project
val mod_version: String by project
val mappings_version: String by project
val mod_author: String by project

val userConfig = Properties()
val cfg = rootProject.file("user.properties")
if (cfg.exists()) userConfig.load(cfg.inputStream())

buildscript {
    dependencies {
        classpath("org.spongepowered:mixingradle:0.7.38")
    }
}

plugins {
    id("net.minecraftforge.gradle")
    id("org.parchmentmc.librarian.forgegradle")
    id("org.jetbrains.kotlin.jvm") version "1.8.21"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "8+"
    `maven-publish`
}

apply(plugin = "org.spongepowered.mixin")

group = mod_group
version = "${minecraft_version}-$mod_version"
project.setProperty("archivesBaseName", mod_id)

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
    kotlinOptions.freeCompilerArgs = listOf("-Xjvm-default=all", "-Xopt-in=kotlin.RequiresOptIn")
}

configure<UserDevExtension> {
    mappings("parchment", "$mappings_version-$minecraft_version")

    accessTransformer("src/main/resources/META-INF/accesstransformer.cfg")

    val defaultConfig = Action<RunConfig> {
        workingDirectory(project.file("run"))
        properties(
            mapOf(
                "forge.logging.markers" to "REGISTRIES",
                "forge.logging.console.level" to "debug"
            )
        )
        //jvmArg("-XX:+AllowEnhancedClassRedefinition")
        arg("-mixin.config=$mod_id.mixins.json")
        mods.create(mod_id) {
            source(the<JavaPluginExtension>().sourceSets.getByName("main"))
        }
    }

    runs.create("client", defaultConfig)
    runs.create("server", defaultConfig)

    runs.all {
        lazyToken("minecraft_classpath") {
            library.copyRecursive().resolve().joinToString(File.pathSeparator) { it.absolutePath }
        }
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://cursemaven.com") }
    maven { url = uri("https://thedarkcolour.github.io/KotlinForForge/") }
    flatDir { dir("libs") }
}

val shadeKotlin by configurations.creating
val library = configurations.create("library")

configurations {
    library.extendsFrom(this["shadow"])
    implementation.get().extendsFrom(library)
    compileOnly.get().extendsFrom(shadeKotlin)
}

dependencies {
    val minecraft = configurations["minecraft"]
    val shadow = configurations["shadow"]

    minecraft("net.minecraftforge:forge:$minecraft_version-$forge_version")

    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    implementation("thedarkcolour:kotlinforforge:3.12.0")
    implementation(fg.deobf("curse.maven:embeddium-908741:4984830"))
    implementation(fg.deobf("curse.maven:oculus-581495:4763262"))
    implementation(fg.deobf("curse.maven:spark-361579:4505309"))
    implementation(fg.deobf("curse.maven:carry-on-274259:4882496"))
    implementation(fg.deobf("curse.maven:ftb-library-forge-404465:4661834"))
    implementation(fg.deobf("curse.maven:architectury-api-419699:4555749"))
    compileOnly(fg.deobf("curse.maven:ftb-teams-forge-404468:4611938"))
    compileOnly(fg.deobf("curse.maven:firstperson:2.3.2"))

    shadow("com.esotericsoftware:kryo:5.4.0")
}

if (System.getProperty("user.name").equals(userConfig.getProperty("user"))) {
    tasks.getByName("build").finalizedBy("copyJar")
}

fun Jar.createManifest() = manifest {
    attributes(
        "Automatic-Module-Name" to mod_id,
        "Specification-Title" to mod_id,
        "Specification-Vendor" to mod_author,
        "Specification-Version" to "1",
        "Implementation-Title" to project.name,
        "Implementation-Version" to version,
        "Implementation-Vendor" to mod_author,
        "Implementation-Timestamp" to ZonedDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")),
        "MixinConfigs" to "$mod_id.mixins.json"
    )
}

configure<MixinExtension> {
    add(sourceSets.main.get(), "hollowcore.refmap.json")
}

val jar = tasks.named<Jar>("jar") {
    archiveClassifier.set("")
    exclude(
        "LICENSE.txt", "META-INF/MANIFSET.MF", "META-INF/maven/**",
        "META-INF/*.RSA", "META-INF/*.SF", "META-INF/versions/**"
    )
    createManifest()
    finalizedBy("reobfJar")
}

//val shadowJar = tasks.named<ShadowJar>("shadowJar") {
//    archiveClassifier.set("")
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    configurations = listOf(library, shadeKotlin)
//
//    exclude(
//        "LICENSE.txt", "META-INF/MANIFSET.MF", "META-INF/maven/**",
//        "META-INF/*.RSA", "META-INF/*.SF", "META-INF/versions/**"
//    )
//
//    dependencies {
//        exclude(dependency("net.java.dev.jna:jna"))
//    }
//
//    exclude("**/module-info.class")
//
//    createManifest()
//
//    finalizedBy("reobfShadowJar")
//}

//(extensions["reobf"] as NamedDomainObjectContainer<*>).create("shadowJar")
//tasks.getByName("build").dependsOn("shadowJar")
//
//tasks {
//    whenTaskAdded {
//        if (name == "prepareRuns") dependsOn(shadowJar)
//    }
//}

val copyJar by tasks.registering(Copy::class) {
    from(jar.flatMap(Jar::getArchiveFile).get().asFile)
    into("../HollowEngine/hc")
}
