plugins {
    val kotlinVersion = "1.5.21"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.6.7"
    id("org.jetbrains.dokka") version "1.5.0"
}

group = "cn.j4ger"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}

dependencies {
    val ktorVersion = "1.6.2"
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-serialization:$ktorVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
    implementation("org.reflections:reflections:0.9.12")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
}
