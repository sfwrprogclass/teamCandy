plugins {
    kotlin("jvm") version "2.3.0"
    application
    kotlin("plugin.serialization") version "1.9.22"
}

group = "edu.teamcandy"
version = "1.0-SNAPSHOT"
val ktorVersion = "3.4.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.50.0")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.0")

    // Sqlite
    implementation("org.xerial:sqlite-jdbc:3.45.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    //Ktor
    implementation("io.ktor:ktor-server-core-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-server-netty-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:${ktorVersion}")
    implementation("io.ktor:ktor-server-cors-jvm:${ktorVersion}")

    //Swagger
    implementation("io.ktor:ktor-server-swagger:${ktorVersion}")
    implementation("io.ktor:ktor-server-openapi:${ktorVersion}")
}

application {
    mainClass = "edu.teamcandy.MainKt"
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}