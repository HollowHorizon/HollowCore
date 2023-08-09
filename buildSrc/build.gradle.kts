repositories {
    maven { url = uri("https://plugins.gradle.org/m2/") }
    mavenCentral()
}

plugins {
    `kotlin-dsl`
}

dependencies {
    implementation("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7+")
    implementation("org.codehaus.plexus:plexus-utils:4.0.0")
    implementation("org.gradle.api.plugins:gradle-nexus-plugin:0.3")
    implementation("dev.gradleplugins:gradle-api:7.5.1")
}