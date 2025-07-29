import net.fabricmc.loom.util.ModPlatform
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.jvm.tasks.Jar
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.named

object PublishingSetup {
    fun setupPublishing(
        project: Project,
        modProject: ModProject,
        minecraftVersion: String,
        modPlatform: String,
        vararg publications: Publication
    ) {
        val publishing = project.extensions["publishing"] as PublishingExtension

        publishing.apply {
            publications {
                create(modProject.modName, MavenPublication::class.java) {
                    groupId = "ru.hollowhorizon"
                    artifactId = "${modProject.modName}-${modPlatform}-${minecraftVersion}"
                    version = modProject.modVersion

                    artifact(project.tasks.named<Jar>("remapJar"))
                    artifact(project.tasks.named<Jar>("remapSourcesJar"))
                    artifact(project.tasks.named<Jar>("jar"))
                }
            }

            repositories {
                publications.forEach { publication ->
                    maven {
                        name = publication.name
                        url = project.uri(publication.url)

                        credentials {
                            username = publication.username
                            password = publication.password
                        }
                    }
                }
                if (System.getenv("MAVEN_PASSWORD") != null) maven {
                    name = "GitHubPackages"
                    url = project.uri("https://maven.pkg.github.com/HollowHorizon/${modProject.modName}")

                    credentials {
                        username = System.getenv("MAVEN_USER") // Имя пользователя
                        password = System.getenv("MAVEN_PASSWORD") // Токен
                    }
                }
                if (System.getenv("MAVEN_PASSWORD_ZM") != null) maven {
                    name = "ZeroModsMaven"
                    url = project.uri("https://maven.0mods.team/releases")

                    credentials {
                        username = System.getenv("MAVEN_USER_ZM") // Имя пользователя
                        password = System.getenv("MAVEN_PASSWORD_ZM") // Токен
                    }
                }
                mavenLocal()
            }

        }
    }
}

data class Publication(val name: String, val url: String, val username: String, val password: String)