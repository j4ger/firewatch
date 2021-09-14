val firewatchVersion = "1.0.2"
val globalKotlinVersion = "1.5.10"

plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.7.0"
    id("maven-publish")
    id("signing")
}

group = "cn.j4ger"
version = firewatchVersion
description = """
    Social media update watcher plugin for Mirai
""".trimIndent()

repositories {
    mavenLocal()
    maven("https://maven.aliyun.com/repository/public")
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    implementation("com.github.kittinunf.fuel:fuel:2.3.1")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:2.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
    implementation("org.reflections:reflections:0.9.12")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

java {
    withJavadocJar()
    withSourcesJar()
}

tasks.jar {
    from(configurations.runtimeClasspath.get().map{
        println("Bundling dependency $it")
        if (it.isDirectory) it else zipTree(it)
    })
    val sourcesMain = sourceSets.main.get()
    from(sourcesMain.output)
}

tasks.register("showDependencies") {
    configurations.runtimeClasspath.get().forEach {
        println("$it")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven"){
            groupId="cn.j4ger"
            artifactId="firewatch"
            version=firewatchVersion

            from(components["java"])

            pom {
                name.set("firewatch")
                description.set("""
                        Social media update watcher plugin for Mirai
                """.trimIndent())
                url.set("https://firewatch.j4ger.cn")
                licenses {
                    license {
                        name.set("AGPL-3.0")
                        url.set("https://www.gnu.org/licenses/agpl-3.0.html")
                    }
                }
                developers{
                    developer{
                        id.set("j4ger" )
                        email.set("xiayuxuan@live.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/j4ger/firewatch.git")
                    developerConnection.set("scm:git:git://github.com/j4ger/firewatch.git")
                    url.set("https://firewatch.j4ger.cn")
                }
            }
            repositories {
                maven {
                    credentials {
                        username=properties["nexusUsername"].toString()
                        password=properties["nexusPassword"].toString()
                    }
                    name="MavenCentral"
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")

                }
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/j4ger/firewatch")
                    credentials {
                        username = properties["githubUsername"].toString()
                        password = properties["githubPassword"].toString()
                    }
                }
            }
        }
    }
}

signing {
    sign(publishing.publications.getByName("maven"))
}
