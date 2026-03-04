plugins {
    kotlin("jvm") version "2.3.0"
    application
}

group = "edu.teamcandy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

application {
    mainClass = "edu.teamcandy.MainKt"
}

kotlin {
    jvmToolchain(22)
}

tasks.test {
    useJUnitPlatform()
}