import com.matthewprenger.cursegradle.CurseArtifact
import com.matthewprenger.cursegradle.CurseProject
import com.matthewprenger.cursegradle.CurseRelation

plugins {
    id("fabric-loom").version("0.8-SNAPSHOT")
    id("com.matthewprenger.cursegradle").version("1.4.0")
}

val env: Map<String, String> = System.getenv()
version = env["MOD_VERSION"] ?: "local"

allprojects {
    apply(plugin = "fabric-loom")

    version = rootProject.version

    dependencies {
        minecraft("com.mojang:minecraft:${rootProp["minecraft"]}")
        mappings("net.fabricmc:yarn:${rootProp["yarn"]}:v2")

        modImplementation("net.fabricmc:fabric-loader:${rootProp["loader"]}")
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_16
        targetCompatibility = JavaVersion.VERSION_16
        withSourcesJar()
    }

    afterEvaluate {
        tasks.processResources {
            inputs.property("version", project.version)

            filesMatching("fabric.mod.json") {
                expand("version" to project.version)
            }
        }

        tasks.withType<JavaCompile> {
            options.encoding = "UTF-8"
            options.release.set(16)
        }

        tasks.jar {
            from(rootProject.file("includes"))
        }
    }
}

subprojects.forEach {
    tasks.remapJar {
        dependsOn(it.tasks.named("remapJar").get())
    }
}

dependencies {
    afterEvaluate {
        subprojects.forEach {
            implementation(it)
            include(it)
        }
    }
}

curseforge {
    env["CURSEFORGE_API"]?.let { CURSEFORGE_API ->
        apiKey = CURSEFORGE_API
        project(closureOf<CurseProject> {
            id = prop["cf.projectId"]
            releaseType = prop["cf.releaseType"]

            changelogType = "markdown"
            changelog = "https://github.com/badasintended/cpas/releases/tag/${project.version}"

            mainArtifact(tasks["remapJar"], closureOf<CurseArtifact> {
                displayName = "[${prop["minecraft"]}] v${project.version}"
            })

            addGameVersion("Fabric")
            prop["cf.gameVersion"].split(", ").forEach {
                addGameVersion(it)
            }

            relations(closureOf<CurseRelation> {
                prop["cf.require"].split(", ").forEach {
                    requiredDependency(it)
                }
                prop["cf.optional"].split(", ").forEach {
                    optionalDependency(it)
                }
            })

            afterEvaluate {
                uploadTask.dependsOn("build")
            }
        })
    }
}
